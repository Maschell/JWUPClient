package de.mas.wupclient.client.operations;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.mas.wupclient.client.WUPClient;
import de.mas.wupclient.client.utils.FEntry;
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
    
    private IoctlOperations ioctl = null; 

    public FSAOperations(WUPClient client) {
        super(client);
        setIoctlOperations(IoctlOperations.IoctlOperationsFactory(client));
    }

    public IoctlOperations getIoctlOperations() {
        return ioctl;
    }

    public void setIoctlOperations(IoctlOperations ioctl) {
        this.ioctl = ioctl;
    }
    
    public Result<Integer> FSA_OpenDir(int handle, String path) throws IOException{
        byte[] inbuffer = new byte[0x520];
        Utils.writeNullTerminatedStringToByteArray(inbuffer, path, 0x04);       
              
        Result<byte[]> res = ioctl.ioctl(handle, 0x0A, inbuffer, 0x293);
        
        ByteBuffer destByteBuffer = ByteBuffer.allocate(0x04);
        destByteBuffer.order(ByteOrder.BIG_ENDIAN);
        destByteBuffer.put(Arrays.copyOfRange(res.getData(), 0x04, 0x08));
        
        return new Result<Integer>(res.getResultValue(),destByteBuffer.getInt(0x00));
    }
    
    public int FSA_CloseDir(int handle, int dirhandle) throws IOException{
        byte[] inbuffer = new byte[0x520];
        Utils.writeIntToByteArray(inbuffer, dirhandle, 0x04);
        Result<byte[]> res = ioctl.ioctl(handle, 0x0D, inbuffer, 0x293);
        return res.getResultValue();
    }
    
    public Result<FEntry> FSA_ReadDir(int fsa_handle, int dirhandle) throws IOException {
        byte[] inbuffer = new byte[0x520];
        Utils.writeIntToByteArray(inbuffer,dirhandle,4);
       
        Result<byte[]> res = ioctl.ioctl(fsa_handle, 0x0B, inbuffer, 0x293);
        
        byte[] unknowndata = Arrays.copyOfRange(res.getData(), 0x04, 0x68);
        
        String filename = Utils.getStringFromByteArray(Arrays.copyOfRange(res.getData(), 0x68, res.getData().length));
        if(res.getResultValue() == 0){
            return new Result<FEntry>(res.getResultValue(),new FEntry(filename,((unknowndata[0] & 0x01) == 0x01),unknowndata));
        }else{
            return new Result<FEntry>(res.getResultValue(),null);
        }   
    }
}
