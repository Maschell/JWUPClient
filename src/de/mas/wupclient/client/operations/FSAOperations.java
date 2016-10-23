package de.mas.wupclient.client.operations;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import de.mas.wupclient.client.WUPClient;
import de.mas.wupclient.client.utils.FEntry;
import de.mas.wupclient.client.utils.Result;

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
    
    public Result<FEntry> FSA_ReadDir(int fsa_handle, int dirhandle) throws IOException {
        byte[] inbuffer = new byte[0x520]; 
        System.arraycopy(getClient().intToByteArray(dirhandle), 0, inbuffer, 4, 4);       
        Result<byte[]> res = ioctl.ioctl(fsa_handle, 0x0B, inbuffer, 0x293);
        
        byte[] rawdata = res.getData();
        byte[] data = new byte[rawdata.length-4];
        System.arraycopy(rawdata, 4, data, 0, rawdata.length-4);        
        byte[] unknowndata = new byte[0x64];
        System.arraycopy(data, 0, unknowndata, 0, 0x64);        
       
        int i = 0;
        while(data[0x64 + i] != 0 && (i +0x64) < data.length){
            i++;
        }
        byte[] stringData = new byte[i];
        boolean isFile = false;
        if((unknowndata[0] & 0x01) == 1){
            isFile = true;
        }
        System.arraycopy(data, 0x64, stringData, 0, i);      
        //System.out.println(new String(stringData, "UTF-8") + ":" + Integer.toBinaryString(unknowndata[0] & 0x01));
        if(res.getResultValue() == 0){
            return new Result<FEntry>(res.getResultValue(),new FEntry(new String(stringData, "UTF-8"),isFile,unknowndata));
        }else{
            return new Result<FEntry>(res.getResultValue(),null);
        }   
    }
    
    public Result<Integer> FSA_OpenDir(int handle, String path) throws IOException{
        byte[] inbuffer = new byte[0x520];
        byte[] string = path.getBytes();
        
        System.arraycopy(string , 0, inbuffer, 4, string.length);
        inbuffer[string.length + 4] = 0;
              
        Result<byte[]> res = ioctl.ioctl(handle, 0x0A, inbuffer, 0x293);
        byte[] data =  res.getData();
        ByteBuffer destByteBuffer = ByteBuffer.allocate(data.length);
        destByteBuffer.order(ByteOrder.BIG_ENDIAN);
        destByteBuffer.put(data);
        
        return new Result<Integer>(res.getResultValue(),destByteBuffer.getInt(4));
    }
    
    public int FSA_CloseDir(int handle, int dirhandle) throws IOException{
        byte[] inbuffer = new byte[0x520]; 
        System.arraycopy(getClient().intToByteArray(dirhandle), 0, inbuffer, 4, 4);
        Result<byte[]> res = ioctl.ioctl(handle, 0x0D, inbuffer, 0x293);
        return res.getResultValue();
    }
    
    

}
