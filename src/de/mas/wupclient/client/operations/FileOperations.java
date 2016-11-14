package de.mas.wupclient.client.operations;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.mas.wupclient.client.WUPClient;
import de.mas.wupclient.client.utils.FEntry;
import de.mas.wupclient.client.utils.FStats;
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
        return fsa.FSA_MakeDir(fsa_handle, path, flags);
    }
    
  
    public boolean cp(String source, String target) throws IOException{
        return copyFile(source, target);
    }
    public boolean cpdir(String source, String destination)throws IOException{
        return cpdir(source, destination,"",".*",false);
    }
    public boolean cpdir(String source, String destination,boolean deepSearch)throws IOException{
        return cpdir(source, destination,"",".*",deepSearch);
    }
    public boolean cpdir(String source, String destination,String relativePath,String pattern,boolean deepSearch)throws IOException{
        System.out.println("Copying folder " +source + " to " + destination);
        List<FEntry> entries = util.ls(source,true);
        for(FEntry entry : entries){
            String new_relativePath = relativePath+ "/" + entry.getFilename();
            String src_filename = source + "/" + entry.getFilename();
            String dst_filename = destination + "/" + entry.getFilename();
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(new_relativePath);
            Matcher m1 = p.matcher(new_relativePath + "/");
            boolean matches = m.matches() || m1.matches();
            if(matches || deepSearch){    
                if(entry.isFile()){
                    if(matches){
                        boolean result = cp(src_filename,dst_filename);
                        if(!result) break;
                    }
                }else{
                    mkdir(dst_filename, 0x600);
                    cpdir(src_filename, dst_filename,new_relativePath,pattern,deepSearch);
                }
            }
        }
        return false;       
    }
                        
   
    public boolean copyFile(String source, String destination) throws IOException{
        int fsa_handle = getClient().get_fsa_handle();
        Result<Integer> result_src = fsa.FSA_OpenFile(fsa_handle, source, "r");
        if(result_src.getResultValue() != 0){
            Logger.logErr("copyFile error: couldn't open " + source);
            return false;
        }
        
        Result<Integer> result_dst = fsa.FSA_OpenFile(fsa_handle, destination, "w");
        if(result_dst.getResultValue() != 0){
            Logger.logErr("copyFile error: couldn't open " + destination);
            fsa.FSA_CloseFile(fsa_handle, result_src.getData());
            return false;
        }
        int block_size = 0x20000;
        int buffer_ptr = system.alloc(block_size, 0x40);
        int i =0;
        int src_handle = result_src.getData();
        int dst_handle = result_dst.getData();
        boolean result = true;
        
        Result<FStats> result_stat = fsa.FSA_StatFile(fsa_handle, src_handle);
        if(result_stat.getResultValue() < 0){
            Logger.log("copyFile error: FSA_StatFile  failed.");
            return false;
        }        
        long file_size = result_stat.getData().getPhyssize();
        System.out.println("Copying " + source + " to " + destination + " ("+ (file_size /1024.0f) +" kb)");
        long startTime = System.currentTimeMillis();
        while(true){
            Result<byte[]> result_read = fsa.FSA_ReadFilePtr(fsa_handle, src_handle, 0x1, block_size, buffer_ptr);
            int result_val = result_read.getResultValue();
            if(result_val < 0){
                Logger.log("copyFile error: reading source file failed.");
                result = false;
                break;
            }
            i += result_val;
            Result<byte[]> result_write = fsa.FSA_WriteFilePtr(fsa_handle, dst_handle, 0x1, result_val, buffer_ptr);
            int result_write_val = result_write.getResultValue();
            if(result_write_val < 0){
                Logger.log("copyFile error: writing destination file failed.");
                result = false;
                break;
            }
            long diff_time = System.currentTimeMillis() - startTime;
          
            System.out.print("" + String.format("Writing File: progress %08X bytes (%02.02f", i,(i*1.0/file_size*1.0)*100) + "%) (" + String.format("%04d",   (int)((i*1.0f) /(diff_time*1.0f)))+" kb/s)\r");
            if(result_val < block_size){
                break;
            }
        }
        System.out.println("Wrote   " + source + " to " + destination + " ("+ (file_size /1024.0f) +" kb)");
        system.free(buffer_ptr);
        fsa.FSA_CloseFile(fsa_handle, src_handle);
        fsa.FSA_CloseFile(fsa_handle, dst_handle);
        return result;
    }
   
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
