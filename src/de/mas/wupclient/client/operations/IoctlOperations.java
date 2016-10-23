package de.mas.wupclient.client.operations;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import de.mas.wupclient.Utils;
import de.mas.wupclient.client.WUPClient;
import de.mas.wupclient.client.utils.Result;

public class IoctlOperations extends Operations {    
    private static Map<WUPClient,IoctlOperations> instances = new HashMap<>();
    public static IoctlOperations IoctlOperationsFactory(WUPClient client){
        if(!instances.containsKey(client)){
            instances.put(client, new IoctlOperations(client));
        }
        return instances.get(client);        
    }
    
    private IoctlOperations(WUPClient client) {
        super(client);
    }
    
    public Result<byte[]> ioctl(int handle,int cmd, byte[] inbuf,int outbuf_size) throws IOException{       
        int in_address = getClient().load_buffer(inbuf);
        byte[] out_data = new byte[0];
        int ret;
        
        if(outbuf_size > 0){
            int out_address = getClient().alloc(outbuf_size);
            int[] buffer =  new int[6];
            buffer[0] = handle;
            buffer[1] = cmd;
            buffer[2] = in_address;
            buffer[3] = inbuf.length;
            buffer[4] = out_address;
            buffer[5] = outbuf_size;           
            ret = getClient().svc(0x38, buffer);
            out_data = getClient().read(out_address, outbuf_size);
            getClient().free(out_address);
        }else{
            int[] buffer =  new int[6];
            buffer[0] = handle;
            buffer[1] = cmd;
            buffer[2] = in_address;
            buffer[3] = inbuf.length;
            buffer[4] = 0;
            buffer[5] = 0;
            ret = getClient().svc(0x38, buffer);
        }
        getClient().free(in_address);
        return new Result<byte[]>(ret,out_data);
    }
    
    public int iovec(int[][] inputData) throws IOException{       
        ByteBuffer destByteBuffer = ByteBuffer.allocate(inputData.length * (0x04*3));
        destByteBuffer.order(ByteOrder.BIG_ENDIAN);
        for (int[] foo : inputData){          
            destByteBuffer.putInt(foo[0]);
            destByteBuffer.putInt(foo[1]);
            destByteBuffer.putInt(0);           
        }          
        return getClient().load_buffer(destByteBuffer.array());
    }
    
    /**
     * UNTESTED!
     */
    public Result<byte[][]> ioctlv(int handle, int cmd,byte[][] inbufs,int[] outbuf_sizes,int[][] ínbufs_ptr,int[][] outbufs_ptr) throws IOException{
        int new_inbufs[][] = new int[inbufs.length][2];
        int i = 0;
        for(byte[] data : inbufs){
            new_inbufs[i][0] = getClient().load_buffer(data, 0x40);
            new_inbufs[i][1] = data.length;
            i++;
        }
        int new_outbufs[][] = new int[outbuf_sizes.length][2];
        i = 0;
        for(int cur_size : outbuf_sizes){
            new_outbufs[i][0] = getClient().alloc(cur_size, 0x40);
            new_outbufs[i][1] = cur_size;
            i++;
        }
            
        int[][] iovecs_buffer =  Utils.concatAll(new_inbufs,ínbufs_ptr,outbufs_ptr,new_outbufs);      

        int iovecs = iovec(iovecs_buffer);
        int[] buffer = new int[5];
        buffer[0] = handle;
        buffer[1] = cmd;
        buffer[2] = new_inbufs.length + ínbufs_ptr.length;
        buffer[3] = new_outbufs.length + outbufs_ptr.length;
        buffer[4] = iovecs;       
        int  ret = getClient().svc(0x39, buffer);
        
        byte[][] out_data = new byte[new_outbufs.length][];
        i=0;
        for (int[] foo : new_outbufs){
            out_data[i] = getClient().read(foo[0],foo[1]);
            i++;
        }
        i=0;
        int[][] free_buffer =  Utils.concatAll(new_inbufs,new_outbufs);      
        for (int[] foo : free_buffer){
            getClient().free(foo[0]);
            i++;
        }
        getClient().free(iovecs);
        return new Result<byte[][]>(ret,out_data);       
    }
}
