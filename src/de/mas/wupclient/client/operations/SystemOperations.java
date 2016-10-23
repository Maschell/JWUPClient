package de.mas.wupclient.client.operations;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import de.mas.wupclient.client.WUPClient;
import de.mas.wupclient.client.utils.Result;
import de.mas.wupclient.client.utils.Utils;

public class SystemOperations extends Operations {    
    private static Map<WUPClient,SystemOperations> instances = new HashMap<>();
    public static SystemOperations SystemOperationsFactory(WUPClient client){
        if(!instances.containsKey(client)){
            instances.put(client, new SystemOperations(client));
        }
        return instances.get(client);        
    }
    
    private SystemOperations(WUPClient client) {
        super(client);
    }
    
    public Result<byte[]> ioctl(int handle,int cmd, byte[] inbuf,int outbuf_size) throws IOException{       
        int in_address = load_buffer(inbuf);
        byte[] out_data = new byte[0];
        int ret;
        
        if(outbuf_size > 0){
            int out_address = alloc(outbuf_size);          
            ret = getClient().svc(0x38, new int[] {handle,cmd,in_address,inbuf.length,out_address,outbuf_size});
            out_data = getClient().read(out_address, outbuf_size);
            free(out_address);
        }else{
            ret = getClient().svc(0x38, new int[] {handle,cmd,in_address,inbuf.length,0,0});
        }
        free(in_address);
        return new Result<byte[]>(ret,out_data);
    }
    
    public int iovec(int[][] inputData) throws IOException{       
        ByteBuffer destByteBuffer = ByteBuffer.allocate(inputData.length * (0x04*3));
        destByteBuffer.order(ByteOrder.BIG_ENDIAN);
        for (int[] data : inputData){          
            destByteBuffer.putInt(data[0]);
            destByteBuffer.putInt(data[1]);
            destByteBuffer.putInt(0);           
        }          
        return load_buffer(destByteBuffer.array());
    }
    
    public Result<byte[][]> ioctlv(int handle, int cmd, byte[][] inbufs, int[] outbuf_sizes) throws IOException {  
        return ioctlv(handle,cmd,inbufs,outbuf_sizes,new int[0][],new int[0][]);
    }
    
    public Result<byte[][]> ioctlv(int handle, int cmd,byte[][] inbufs,int[] outbuf_sizes,int[][] ínbufs_ptr,int[][] outbufs_ptr) throws IOException{
        int new_inbufs[][] = new int[inbufs.length][2];
        int i = 0;
        for(byte[] data : inbufs){
            new_inbufs[i][0] = load_buffer(data, 0x40);
            new_inbufs[i][1] = data.length;
            i++;
        }
        int new_outbufs[][] = new int[outbuf_sizes.length][2];
        i = 0;
        for(int cur_size : outbuf_sizes){
            new_outbufs[i][0] = alloc(cur_size, 0x40);
            new_outbufs[i][1] = cur_size;
            i++;
        }
            
        int iovecs = iovec(Utils.concatAll(new_inbufs,ínbufs_ptr,outbufs_ptr,new_outbufs));

        int  ret = getClient().svc(0x39, new int[]{handle,cmd,new_inbufs.length + ínbufs_ptr.length,new_outbufs.length + outbufs_ptr.length,iovecs});
        
        byte[][] out_data = new byte[new_outbufs.length][];
        i=0;
        for (int[] foo : new_outbufs){
            out_data[i] = getClient().read(foo[0],foo[1]);
            i++;
        }
        i=0;
        int[][] free_buffer =  Utils.concatAll(new_inbufs,new_outbufs);      
        for (int[] foo : free_buffer){
            free(foo[0]);
            i++;
        }
        free(iovecs);
        return new Result<byte[][]>(ret,out_data);       
    }

   
    public int alloc(int size) throws IOException{
        return alloc(size,0x00);
    }
    
    public int alloc(int size, int align) throws IOException{
        if(size == 0){
            return 0;
        }
        if(align == 0){ 
            int result = getClient().svc(0x27, new int[] {0xCAFF,size});
            return result;
        }else{
            int result = getClient().svc(0x28, new int[] {0xCAFF,size,align});
            return result;
        }
    }
    
    public int free(int address) throws IOException{
        if(address == 0){
            return 0;
        }
        return getClient().svc(0x29, new int[] {0xCAFF,address});      
    }
    
    public int load_buffer(byte[] data) throws IOException{
        return load_buffer(data,0x00);
    }
    
    public int load_buffer(byte[] data,int align) throws IOException{
        if(data.length == 0)
            return 0;
        
        int address = alloc(data.length, align);
        getClient().write(address, data);
        return address;
        
    }
    
    public int load_string(String b) throws IOException{
        return load_string(b,0x00);
    }
    
    public int load_string(String b,int align) throws IOException{
        byte[] buffer = b.getBytes();
        byte[] newBuffer = new byte[buffer.length+1];
        System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
        newBuffer[buffer.length] = 0;
        
        int result = load_buffer(newBuffer, align);
        return result;
    }
    
    public int open(String device, int mode) throws IOException{
        int address = load_string(device);      
        int handle = getClient().svc(0x33,  new int[] {address,mode});
        free(address);
        return handle;
    }
    
    public int close(int handle) throws IOException{
        return getClient().svc(0x34,new int[]{handle});
    }  
    
}
