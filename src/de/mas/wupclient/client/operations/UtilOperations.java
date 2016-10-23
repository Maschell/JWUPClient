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

public class UtilOperations extends Operations {
    private static Map<WUPClient,UtilOperations> instances = new HashMap<>();
    public static UtilOperations UtilOperationsFactory(WUPClient client){
        if(!instances.containsKey(client)){
            instances.put(client, new UtilOperations(client));
        }
        return instances.get(client);        
    }
    
    private FSAOperations fsa = null; 
    
    public UtilOperations(WUPClient client) {
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
    public FSAOperations getFSAOperations() {
        return fsa;
    }
    public void setFSAOperations(FSAOperations fsa) {
        this.fsa = fsa;
    }
}
