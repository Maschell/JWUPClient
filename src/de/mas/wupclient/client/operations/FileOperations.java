package de.mas.wupclient.client.operations;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.mas.wupclient.client.WUPClient;
import de.mas.wupclient.client.utils.Logger;
import de.mas.wupclient.client.utils.Result;

public class FileOperations extends Operations {
   

    private static Map<WUPClient,FileOperations> instances = new HashMap<>();
    public static FileOperations FileOperationsFactory(WUPClient client){
        if(!instances.containsKey(client)){
            instances.put(client, new FileOperations(client));
        }
        return instances.get(client);        
    }
    
    private UtilOperations util = null;
    private FSAOperations fsa = null;
    private SystemOperations system = null; 
    
    public FileOperations(WUPClient client) {
        super(client);    
        setUtil(UtilOperations.UtilOperationsFactory(client));
        setFsa(FSAOperations.FSAOperationsFactory(client));
        setSystem(SystemOperations.SystemOperationsFactory(client));
    }
    public int mkdir(String path, int flags) throws IOException{
        return createDir(path, flags);
    }
    public int createDir(String path, int flags) throws IOException{
        int fsa_handle = getClient().get_fsa_handle();
        return fsa.FSA_MakeDir(fsa_handle, path, 2);
    }
    
    /* FSA_ReadFilePtr needs to be implemented
    public boolean cp(String source, String target) throws IOException{
        return copyFile(source, target);
    }
    
   
    public boolean copyFile(String source, String destination) throws IOException{
        int fsa_handle = getClient().get_fsa_handle();
        Result<Integer> result_src = fsa.FSA_OpenFile(fsa_handle, source, "r");
        if(result_src.getResultValue() != 0){
            Logger.logErr("copyFile error: couldn't open " + source);
            return false;
        }
        Result<Integer> result_dst = fsa.FSA_OpenFile(fsa_handle, destination, "r");
        if(result_dst.getResultValue() != 0){
            Logger.logErr("copyFile error: couldn't open " + destination);
            fsa.FSA_CloseFile(fsa_handle, result_src.getData());
            return false;
        }
        int block_size = 0x10000;
        int buffer_ptr = system.alloc(block_size, 0x40);
        int i =0;
        int src_handle = result_src.getData();
        int dst_handle = result_dst.getData();
        boolean result = true;
        while(true){
            Result<Integer> result_read = fsa.FSA_ReadFilePtr(fsa_handle, src_handle, 0x1, block_size, buffer_ptr);
            if(result_read.getResultValue() < 0){
                Logger.log("copyFile error: reading source file failed.");
                result = false;
                break;
            }
        }
        system.free(buffer_ptr);
        fsa.FSA_CloseFile(fsa_handle, src_handle);
        fsa.FSA_CloseFile(fsa_handle, dst_handle);
        return result;
    }*/
   
    public UtilOperations getUtil() {
        return util;
    }

    public void setUtil(UtilOperations util) {
        this.util = util;
    }

    public FSAOperations getFsa() {
        return fsa;
    }

    public void setFsa(FSAOperations fsa) {
        this.fsa = fsa;
    }
    public SystemOperations getSystem() {
        return system;
    }
    public void setSystem(SystemOperations system) {
        this.system = system;
    }
}
