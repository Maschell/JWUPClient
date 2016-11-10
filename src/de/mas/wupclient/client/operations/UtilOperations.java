package de.mas.wupclient.client.operations;

import java.io.IOException;
import java.util.ArrayList;
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
    private SystemOperations system = null;     
   
    private UtilOperations(WUPClient client) {
        super(client);
        setFSAOperations(FSAOperations.FSAOperationsFactory(client));
        setSystem(SystemOperations.SystemOperationsFactory(client));
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
                //Logger.logCmd("ls(" + targetPath + ")");
            }else{
                //Logger.logCmd("ls()");
            }
        }
        List<FEntry> results = new ArrayList<>();
        int fsa_handle = getClient().get_fsa_handle();
        String path = targetPath;
        if(targetPath == null || targetPath.isEmpty()){
            path = getClient().getCwd();
        }else if(!targetPath.startsWith("/")){
            path = getClient().getCwd() + "/" + targetPath;
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
        getClient().FSA_Close(getClient().get_fsa_handle());
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
    
    public void dump_syslog() throws IOException {
        int syslog_address = Utils.bigEndianByteArrayToInt(getClient().read(0x05095ECC, 4)) + 0x10;
        int block_size = 0x400;
        for(int i = 0;i<0x40000;i += block_size){
            byte[] data = getClient().read(syslog_address + i, block_size);
            String log = Utils.getStringFromByteArray(data);
            if(!log.isEmpty()){
                System.out.println(log);
            }else{
                break;
            }
        }
        getClient().FSA_Close(getClient().get_fsa_handle());
    }
    public boolean cd() throws IOException {
        return cd("");
    }
    public boolean cd(String path) throws IOException {
        if(path.equals(".")){
            return true;
        }
        if(path.equals("..")){
            path = Utils.getParentDir(getClient().getCwd());
        }
        if(!path.startsWith("/")&& getClient().getCwd().startsWith("/")){
            return cd(getClient().getCwd() + "/" + path);
        }
        int fsa_handle = getClient().get_fsa_handle();
        String usedPath = path;
        if(path.equals("..")){
            usedPath = Utils.getParentDir(getClient().getCwd());
            System.out.println(usedPath +" dd");
        }else if (path.isEmpty()){
            usedPath = getClient().getCwd();
        }
        boolean final_result = false;
        Result<Integer> result = fsa.FSA_OpenDir(fsa_handle,usedPath);
        if(result.getResultValue() == 0){
            getClient().setCwd(usedPath);
            fsa.FSA_CloseDir(fsa_handle, result.getData());
            final_result = true;
        }else{
            Logger.logErr("path does not exists");
            final_result = false;
        }
        getClient().FSA_Close(getClient().get_fsa_handle());
        return final_result;
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
    
    public FSAOperations getFSAOperations() {
        return fsa;
    }
    public void setFSAOperations(FSAOperations fsa) {
        this.fsa = fsa;
    }
    
    public SystemOperations getSystem() {
        return system;
    }
    public void setSystem(SystemOperations system) {
        this.system = system;
    }
}
