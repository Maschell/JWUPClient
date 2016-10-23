package de.mas.wupclient.client.operations;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.mas.wupclient.client.WUPClient;
import de.mas.wupclient.client.utils.FEntry;
import de.mas.wupclient.client.utils.Logger;
import de.mas.wupclient.client.utils.Result;
import de.mas.wupclient.client.utils.Utils;

public class FSAOperations extends Operations {
    private static Map<WUPClient,FSAOperations> instances = new HashMap<>();
    public static FSAOperations FSAOperationsFactory(WUPClient client){
        if(!instances.containsKey(client)){
            instances.put(client, new FSAOperations(client));
        }
        return instances.get(client);        
    }
    
    private SystemOperations system = null; 

    private FSAOperations(WUPClient client) {
        super(client);
        setIoctlOperations(SystemOperations.SystemOperationsFactory(client));
    }

    public SystemOperations getIoctlOperations() {
        return system;
    }

    public void setIoctlOperations(SystemOperations ioctl) {
        this.system = ioctl;
    }
    
    public Result<Integer> FSA_OpenDir(int handle, String path) throws IOException{
        byte[] inbuffer = new byte[0x520];
        Utils.writeNullTerminatedStringToByteArray(inbuffer, path, 0x04);       
              
        Result<byte[]> res = system.ioctl(handle, 0x0A, inbuffer, 0x293);
        
        return new Result<Integer>(res.getResultValue(),Utils.bigEndianByteArrayToInt(Arrays.copyOfRange(res.getData(), 0x04, 0x08)));
    }
    
    public int FSA_CloseDir(int handle, int dirhandle) throws IOException{
        byte[] inbuffer = new byte[0x520];
        Utils.writeIntToByteArray(inbuffer, dirhandle, 0x04);
        Result<byte[]> res = system.ioctl(handle, 0x0D, inbuffer, 0x293);
        return res.getResultValue();
    }
    
    public Result<FEntry> FSA_ReadDir(int fsa_handle, int dirhandle) throws IOException {
        byte[] inbuffer = new byte[0x520];
        Utils.writeIntToByteArray(inbuffer,dirhandle,4);
       
        Result<byte[]> res = system.ioctl(fsa_handle, 0x0B, inbuffer, 0x293);
       
        byte[] unknowndata = Arrays.copyOfRange(res.getData(), 0x04, 0x68);
        String filename = Utils.getStringFromByteArray(Arrays.copyOfRange(res.getData(), 0x68, res.getData().length));
        if(res.getResultValue() == 0){
            return new Result<FEntry>(res.getResultValue(),new FEntry(filename,((char)unknowndata[0] & 128) != 128,unknowndata));
        }else{
            return new Result<FEntry>(res.getResultValue(),null);
        }   
    }
    
    public int FSA_Mount(int handle, String device_path, String volume_path, int flags) throws IOException{
        Logger.logCmd("Mounting " + device_path + " to " + volume_path);
        byte[] inbuffer = new byte[0x520];
        Utils.writeNullTerminatedStringToByteArray(inbuffer, device_path, 0x0004);
        Utils.writeNullTerminatedStringToByteArray(inbuffer, volume_path, 0x0284);
        Utils.writeIntToByteArray(inbuffer,flags,0x0504);
       
        Result<byte[][]> result = system.ioctlv(handle, 0x01, new byte[][] {inbuffer,new byte[0]}, new int[]{0x293});
        return result.getResultValue();
    }
    
    public int FSA_Unmount(int handle, String volume_path, int flags) throws IOException{  
        Logger.logCmd("Unmounting " + volume_path);
        byte[] inbuffer = new byte[0x520];
        Utils.writeNullTerminatedStringToByteArray(inbuffer, volume_path, 0x04);
        Utils.writeIntToByteArray(inbuffer,flags,0x284);
        Result<byte[]> result = system.ioctl(handle,0x02,inbuffer,0x293);
        return result.getResultValue();
    }
    
    public int FSA_MakeDir(int handle, String path, int flags) throws IOException{  
        byte[] inbuffer = new byte[0x520];
        Utils.writeNullTerminatedStringToByteArray(inbuffer, path, 0x04);
        Utils.writeIntToByteArray(inbuffer,flags,0x284);
        Result<byte[]> result = system.ioctl(handle, 0x07, inbuffer, 0x293);
        return result.getResultValue();
    }
    
    public Result<Integer> FSA_OpenFile(int handle, String path, String mode) throws IOException{
        byte[] inbuffer = new byte[0x520];
        Utils.writeNullTerminatedStringToByteArray(inbuffer, path, 0x04);
        Utils.writeNullTerminatedStringToByteArray(inbuffer, mode, 0x284);
        Result<byte[]> result = system.ioctl(handle, 0x0E, inbuffer, 0x293);
        
        return new Result<Integer>(result.getResultValue(),Utils.bigEndianByteArrayToInt(Arrays.copyOfRange(result.getData(), 0x04, 0x08)));
    }
    
    public int FSA_CloseFile(int handle, int file_handle) throws IOException{
        byte[] inbuffer = new byte[0x520];
        Utils.writeIntToByteArray(inbuffer, file_handle, 0x04);
        Result<byte[]> res = system.ioctl(handle, 0x15, inbuffer, 0x293);
        return res.getResultValue();
    }     
    
    public Result<byte[]> FSA_ReadFile(int handle, int file_handle, int size, int cnt) throws IOException{
        byte[] inbuffer = new byte[0x520];
        Utils.writeIntToByteArray(inbuffer, size, 0x08);
        Utils.writeIntToByteArray(inbuffer, cnt, 0x0C);
        Utils.writeIntToByteArray(inbuffer, file_handle, 0x14);
        Result<byte[][]> result = system.ioctlv(handle, 0x0F, new byte[][] {inbuffer}, new int[]{size * cnt,0x293});
        
        return new Result<byte[]>(result.getResultValue(),result.getData()[0]);
    }
}
