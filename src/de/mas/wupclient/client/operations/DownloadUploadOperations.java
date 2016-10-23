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
    
    public boolean downloadFolder(String path) throws IOException{
        return downloadFolder(path,null,false);
    }
    
    public boolean downloadFolder(String sourcePath,String targetPath) throws IOException{
        return downloadFolder(sourcePath,targetPath,false);
    }  
    
    public boolean downloadFolder(String sourcePath, String targetPath,boolean useRelativPath) throws IOException {
        List<FEntry> files = util.ls(sourcePath,true);
        if(targetPath == null || targetPath.isEmpty()){
            targetPath = sourcePath;
        }
        for(FEntry f: files){            
            if(f.isFile()){
                downloadFile(sourcePath, f.getFilename(),targetPath);
            }else{
                downloadFolder(sourcePath + "/" + f.getFilename(), targetPath,useRelativPath);
            }
        }
        return false;
    }    
    
    public byte[] downloadFileToByteArray(String path) throws IOException{
        Logger.logCmd("Downloading " + path);
        int fsa_handle = getClient().get_fsa_handle();
        Result<Integer> res = fsa.FSA_OpenFile(fsa_handle, path, "r");        
        boolean success = false;
        byte[] result = null;
        if(res.getResultValue() == 0){
            int block_size = 0x400;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            success = true;
            while(true){
                Result<byte[]> read_result= fsa.FSA_ReadFile(fsa_handle, res.getData(), 0x1, block_size);
               
                if(read_result.getResultValue() <0){
                    Logger.logErr("FSA_ReadFile returned " + read_result.getResultValue());
                    success = false;
                    break;
                }
                
                out.write(Arrays.copyOf(read_result.getData(), read_result.getResultValue()));
                
                if(read_result.getResultValue() <= 0)
                    break;
            }
            fsa.FSA_CloseFile(fsa_handle, res.getData());
            if(success){
                result = out.toByteArray();                
            }
        }
        return result;
    }

    public boolean downloadFile(String sourcePath, String filename, String targetPath) throws IOException {
        return downloadFile(sourcePath, filename, targetPath,null);        
    }
    
    public boolean downloadFile(String sourcePath,String sourceFilename,String targetPath,String targetFileName) throws IOException {
        byte[] data = downloadFileToByteArray(sourcePath + "/" + sourceFilename);
        if(data == null){
            System.out.println("failed");
            return false;            
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
        
        Utils.createSubfolder(subdir);
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
