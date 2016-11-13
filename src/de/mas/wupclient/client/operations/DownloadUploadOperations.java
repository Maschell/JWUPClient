package de.mas.wupclient.client.operations;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mas.wupclient.client.WUPClient;
import de.mas.wupclient.client.utils.FEntry;
import de.mas.wupclient.client.utils.Logger;
import de.mas.wupclient.client.utils.Result;
import de.mas.wupclient.client.utils.Utils;

public class DownloadUploadOperations extends Operations {
    private static Map<WUPClient,DownloadUploadOperations> instances = new HashMap<>();
    public static DownloadUploadOperations DownloadUploadOperationsFactory(WUPClient client){
        if(!instances.containsKey(client)){
            instances.put(client, new DownloadUploadOperations(client));
        }
        return instances.get(client);        
    }
    
    private UtilOperations util = null;
    private FSAOperations fsa = null; 

    public DownloadUploadOperations(WUPClient client) {
        super(client);
        setUtil(UtilOperations.UtilOperationsFactory(client));    
        setFSA(FSAOperations.FSAOperationsFactory(client));
    }
    
    public boolean downloadFolder(String sourcePath) throws IOException{
        return downloadFolder(sourcePath,null,false);
    }    
    public boolean downloadFolder(String sourcePath, String targetPath,boolean fullpath) throws IOException {
        String new_source_path = sourcePath;
        
        if(!sourcePath.isEmpty() && !sourcePath.startsWith("/")){
            new_source_path = getClient().getCwd() + "/"+  sourcePath;
        }else if(sourcePath == null || sourcePath.isEmpty()){
            new_source_path = getClient().getCwd();
        }
        
        if(targetPath == null || targetPath.isEmpty()){
            if(fullpath){
                targetPath = new_source_path;
            }else{
                targetPath = "";
                if(!sourcePath.isEmpty() && !sourcePath.startsWith("/")){
                    targetPath = sourcePath;
                }
            }            
        }else{
            if(fullpath){
                targetPath += new_source_path;
            }else{
                if(!sourcePath.isEmpty() && !sourcePath.startsWith("/")){
                    targetPath += "/" + sourcePath;
                }
            }
        }
        return _downloadFolder(new_source_path,targetPath);
    }
    private boolean _downloadFolder(String sourcePath, String targetPath) throws IOException {
        Logger.logCmd("Downloading folder " + sourcePath);
              
        List<FEntry> files = util.ls(sourcePath,true);   
        
        Utils.createSubfolder(targetPath);
        
        for(FEntry f: files){            
            if(f.isFile()){
                //System.out.println(targetPath);
                downloadFile(sourcePath, f.getFilename(),targetPath);
            }else{
                _downloadFolder(sourcePath + "/" + f.getFilename(), targetPath + "/" + f.getFilename());
            }
        }
        return false;
    }    
    
    public byte[] downloadFileToByteArray(String path) throws IOException{     
        int fsa_handle = getClient().get_fsa_handle();
        Result<Integer> res = fsa.FSA_OpenFile(fsa_handle, path, "r");        
        boolean success = false;
        byte[] result = null;
        if(res.getResultValue() == 0){
            int block_size = 0x400;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            success = true;
            int total_read = 0;
            while(true){
                Result<byte[]> read_result= fsa.FSA_ReadFile(fsa_handle, res.getData(), 0x1, block_size);
                
                if(read_result.getResultValue() <0){
                    Logger.logErr("FSA_ReadFile returned " + read_result.getResultValue());
                    success = false;
                    break;
                }
                total_read += read_result.getResultValue();
                out.write(Arrays.copyOf(read_result.getData(), read_result.getResultValue()));
                
                if(read_result.getResultValue() <= 0)
                    break;
                if((total_read /1024) % 50 == 0){
                    System.out.print("Downloading file: " + String.format("%.3f", (double)(total_read /1024.0)) + " kb done\r");
                }
            }           
            fsa.FSA_CloseFile(fsa_handle, res.getData());
            if(success){
                System.out.println("Download done: " + total_read+ " bytes");
                result = out.toByteArray();                
            }
        }
        return result;
    }
    
    public boolean downloadFile(String sourcePath, String filename) throws IOException {
        return downloadFile(sourcePath, filename, "");
    }

    public boolean downloadFile(String sourcePath, String filename, String targetPath) throws IOException {
        return downloadFile(sourcePath, filename, targetPath,null);        
    }
    
    public boolean downloadFile(String sourcePath,String sourceFilename,String targetPath,String targetFileName) throws IOException {
       
        if(sourcePath == null || sourcePath.isEmpty()){
            sourcePath = getClient().getCwd();
        }
        if(targetPath == null || targetPath.isEmpty()){
            targetPath = "";
        }
        
        String  subdir = "";        
       
        if(targetFileName == null){
            subdir += targetPath + "/" + sourceFilename;
        }else{
            subdir += targetPath + "/" + targetFileName;
        }
        if(subdir.startsWith("/")){
            subdir = subdir.substring(1);
        }
        if(!targetPath.isEmpty()){
            Utils.createSubfolder(subdir);
        }
        Logger.logCmd("Downloading " + sourcePath + "/" + sourceFilename + " to " + subdir);
        
        byte[] data = downloadFileToByteArray(sourcePath + "/" + sourceFilename);
        if(data == null){
            System.out.println("failed");
            return false;            
        }
        
        FileOutputStream stream = new FileOutputStream(subdir);
        try {
            stream.write(data);
        } finally {
            stream.close();
        }
        return true;        
    }

    public UtilOperations getUtil() {
        return util;
    }

    public void setUtil(UtilOperations util) {
        this.util = util;
    }
    
    public FSAOperations getFSA() {
        return fsa;
    }

    public void setFSA(FSAOperations fsa) {
        this.fsa = fsa;
    }    
}
