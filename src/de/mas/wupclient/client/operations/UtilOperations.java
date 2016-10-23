package de.mas.wupclient.client.operations;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mas.wupclient.client.WUPClient;
import de.mas.wupclient.client.utils.FEntry;
import de.mas.wupclient.client.utils.Logger;
import de.mas.wupclient.client.utils.Result;
import de.mas.wupclient.client.utils.Utils;

public class UtilOperations extends Operations {
    private static Map<WUPClient,UtilOperations> instances = new HashMap<>();
    public static UtilOperations UtilOperationsFactory(WUPClient client){
        if(!instances.containsKey(client)){
            instances.put(client, new UtilOperations(client));
        }
        return instances.get(client);        
    }
    
    private FSAOperations fsa = null; 
    
    private UtilOperations(WUPClient client) {
        super(client);
        setFSAOperations(FSAOperations.FSAOperationsFactory(client));
    }
    public List<FEntry> ls() throws IOException{
        return ls(null,false);
    }
    public List<FEntry> ls(boolean return_data) throws IOException{
        return ls(null,return_data);
    }
    public List<FEntry> ls(String targetPath) throws IOException{
        return ls(targetPath,false);
    }
    public List<FEntry> ls(String targetPath,boolean return_data) throws IOException{
        if(!return_data){
            if(targetPath != null){
                Logger.logCmd("ls(" + targetPath + ")");
            }else{
                Logger.logCmd("ls()");
            }
        }
        List<FEntry> results = new ArrayList<>();
        int fsa_handle = getClient().get_fsa_handle();
        String path = targetPath;
        if(targetPath == null || targetPath.isEmpty()){
            path = getClient().getCwd();
        }
        
        Result<Integer> res = fsa.FSA_OpenDir(fsa_handle, path);
        if(res.getResultValue() != 0x0){
           Logger.logErr("opendir error : " + String.format("%08X",res.getResultValue()));
        }
        
        int dirhandle = res.getData();
        while(true){      
            Result<FEntry> result = fsa.FSA_ReadDir(fsa_handle, dirhandle);
            if (result.getResultValue() != 0){
                break;
            }
            FEntry entry = result.getData();
            if(entry == null){
                break;
            }
            if(!return_data){
                if(entry.isFile()){
                    Logger.log(entry.getFilename());               
                }else{
                    Logger.log(entry.getFilename() + "/");
                }
            }else{
                results.add(entry);
            }
        }
        int result;
        if((result = fsa.FSA_CloseDir(fsa_handle, dirhandle)) != 0){
            Logger.logErr("CloseDir failed!" + result);
        }
        return results;
    }
    
    public void lsRecursive() throws IOException {
        lsRecursive(getClient().getCwd() + "/");        
    }
    
    public void lsRecursive(String path) throws IOException{        
        List<FEntry> result = ls(path,true);
        for(FEntry entry : result){
            if(entry.isFile()){
                Logger.log(path + entry.getFilename());
            }else{
                String newPath = path + entry.getFilename() + "/";
                Logger.log(newPath);
                lsRecursive(newPath);
            }
        }
    }
    
    public int mount(String device_path, String volume_path, int flags) throws IOException{        
        int fsa_handle = getClient().get_fsa_handle();
        return fsa.FSA_Mount(fsa_handle, device_path, volume_path, 2);
    }
    public int unmount(String volume_path, int flags) throws IOException{        
        int fsa_handle = getClient().get_fsa_handle();
        return fsa.FSA_Unmount(fsa_handle, volume_path, 2);
    }
    
    public int mount_sdcard() throws IOException{        
        return mount("/dev/sdcard01", "/vol/storage_sdcard", 2);
    }
    
    public int unmount_sdcard() throws IOException{
        return unmount("/vol/storage_sdcard", 2);
    }
    
    public int mount_odd_content() throws IOException{      
        return mount("/dev/odd03", "/vol/storage_odd_content", 2);
    }
    
    public int unmount_odd_content() throws IOException{
        return unmount("/vol/storage_odd_content", 2);
    }
    
    public int mount_odd_ticket() throws IOException{      
        return mount("/dev/odd01", "/vol/storage_odd_ticket", 2);
    }
    
    public int unmount_odd_ticket() throws IOException{
        return unmount("/vol/storage_odd_ticket", 2);
    }
    
    public int mkdir(String path, int flags) throws IOException{
        int fsa_handle = getClient().get_fsa_handle();
        return fsa.FSA_MakeDir(fsa_handle, path, 2);
    }
    
    public boolean downloadFolder(String path) throws IOException{
        return downloadFolder(path,null,false);
    }
    
    public boolean downloadFolder(String sourcePath,String targetPath) throws IOException{
        return downloadFolder(sourcePath,targetPath,false);
    }  
    
    public boolean downloadFolder(String sourcePath, String targetPath,boolean useRelativPath) throws IOException {
        List<FEntry> files = ls(sourcePath,true);
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
        //System.out.println(subdir);
        Utils.createSubfolder(subdir);
        FileOutputStream stream = new FileOutputStream(subdir);
        try {
            stream.write(data);
        } finally {
            stream.close();
        }
        return true;        
    }   
    
    public FSAOperations getFSAOperations() {
        return fsa;
    }
    public void setFSAOperations(FSAOperations fsa) {
        this.fsa = fsa;
    }
}
