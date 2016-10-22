package de.mas.wupclient;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WUPClient {
    private String IP;
    private int fsaHandle = -1;
    private String cwd = "";
    private Socket socket;    

    public WUPClient(String ip){
        setIP(ip);
        createSocket(ip);
        setFsaHandle(-1);
        setCwd("/vol/storage_mlc01");
    }
    
    public Result send(int command, byte[] data) throws IOException{
        DataOutputStream outToServer = new DataOutputStream(getSocket().getOutputStream());
        ByteBuffer destByteBuffer = ByteBuffer.allocate(data.length + 4);        
        destByteBuffer.order(ByteOrder.BIG_ENDIAN);
        destByteBuffer.putInt(command);
        destByteBuffer.put(data);
        try {
            outToServer.write(destByteBuffer.array());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        destByteBuffer.clear();
        destByteBuffer = ByteBuffer.allocate(0x600);
        byte[] result =  new byte[0x0600];
        int size = getSocket().getInputStream().read(result);
        destByteBuffer.put(result, 0, size);
        int returnValue = destByteBuffer.getInt();
        return new Result(returnValue,Arrays.copyOfRange(result, 4,result.length));        
   }
    
   public byte[] read(int addr, int len) throws IOException{
       ByteBuffer destByteBuffer = ByteBuffer.allocate(8);
       destByteBuffer.order(ByteOrder.BIG_ENDIAN);
       destByteBuffer.putInt(addr);
       destByteBuffer.putInt(len);
       Result result = send(1,destByteBuffer.array());
       if(result.getResultValue() == 0){
           return result.getData();
       }else{
           System.out.println("Read error: " + result.getResultValue());
           return null;
       }
   }
   
   public int write(int addr, byte[] data ) throws IOException{       
       ByteBuffer destByteBuffer = ByteBuffer.allocate(4 + data.length);
       destByteBuffer.order(ByteOrder.BIG_ENDIAN);
       destByteBuffer.putInt(addr);
       destByteBuffer.put(data);
       
       Result result = send(0,destByteBuffer.array());
       if(result.getResultValue() == 0){
           return result.getResultValue();
       }else{
           System.out.println("write error: " + result.getResultValue());
           return -1;
       }
   }
   
   public int svc(int svc_id, int[] arguments) throws IOException{
       ByteBuffer destByteBuffer = ByteBuffer.allocate(4 + (arguments.length * 0x04));
       destByteBuffer.order(ByteOrder.BIG_ENDIAN);
       destByteBuffer.putInt(svc_id);
       for(int i = 0;i<arguments.length;i++){
           destByteBuffer.putInt(arguments[i]);
       }
       
       Result result = send(2,destByteBuffer.array());
       if(result.getResultValue() == 0){
           destByteBuffer.clear();
           destByteBuffer =  ByteBuffer.allocate(result.getData().length);
           destByteBuffer.rewind();
           destByteBuffer.order(ByteOrder.BIG_ENDIAN);
           destByteBuffer.put(result.getData());
           return destByteBuffer.getInt(0);
       }else{
           System.out.println("svc error: " + result.getResultValue());
           return -1;
       }
   }

   public int kill() throws IOException{
       Result result = send(3,new byte[0]);
       return result.getResultValue();
   }
   
   public int memcpy(int dest, int source, int len) throws IOException{
       ByteBuffer destByteBuffer = ByteBuffer.allocate(12);
       destByteBuffer.order(ByteOrder.BIG_ENDIAN);
       destByteBuffer.putInt(dest);
       destByteBuffer.putInt(source);
       destByteBuffer.putInt(len);
       Result result = send(4,destByteBuffer.array());
       if(result.getResultValue() == 0){
           return result.getResultValue();
       }else{
           System.out.println("memcpy error: " + result.getResultValue());
           return -1;
       }
   }
   public int alloc(int size) throws IOException{
       return alloc(size,0x00);
   }
   public int alloc(int size, int align) throws IOException{
       if(size == 0){
           return 0;
       }
       if(align == 0){
           int[] buffer = new int[2];
           buffer[0] = 0xCAFF;
           buffer[1] = size;      
           int result = svc(0x27, buffer);
           return result;
       }else{
           int[] buffer = new int[3];
           buffer[0] = 0xCAFF;
           buffer[1] = size; 
           buffer[2] = align; 
           int result = svc(0x28, buffer);
           return result;
       }
   }
   public int free(int address) throws IOException{
       if(address == 0){
           return 0;
       }      
       int[] buffer = new int[2];
       buffer[0] = 0xCAFF;
       buffer[1] = address;           
       return svc(0x29, buffer);      
   }
   
   public int load_buffer(byte[] data) throws IOException{
       return load_buffer(data,0x00);
   }
   public int load_buffer(byte[] data,int align) throws IOException{
       if(data.length == 0)
           return 0;
       
       int address = alloc(data.length, align);
       write(address, data);
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
       
       int[] buffer = new int[2];
       buffer[0] = address;
       buffer[1] = mode;
      
       int handle = svc(0x33, buffer);
       free(address);
       return handle;
   }
   public int close(int handle) throws IOException{
       int[] buffer = new int[1];
       buffer[0] =  handle;
       return svc(0x34,buffer);
   }  
   public Result ioctl(int handle,int cmd, byte[] inbuf,int outbuf_size) throws IOException{
      
       int in_address = load_buffer(inbuf);
       byte[] out_data = new byte[0];
       int ret;
       
       if(outbuf_size > 0){
           int out_address = alloc(outbuf_size);
           int[] buffer =  new int[6];
           buffer[0] = handle;
           buffer[1] = cmd;
           buffer[2] = in_address;
           buffer[3] = inbuf.length;
           buffer[4] = out_address;
           buffer[5] = outbuf_size;           
           ret = svc(0x38, buffer);
           out_data = read(out_address, outbuf_size);
           free(out_address);
       }else{
           int[] buffer =  new int[6];
           buffer[0] = handle;
           buffer[1] = cmd;
           buffer[2] = in_address;
           buffer[3] = inbuf.length;
           buffer[4] = 0;
           buffer[5] = 0;
           ret = svc(0x38, buffer);
       }
       free(in_address);
       return new Result(ret,out_data);
   }
   
   public int iovec(int[][] inputData) throws IOException{       
       ByteBuffer destByteBuffer = ByteBuffer.allocate(inputData.length * (0x04*3));
       destByteBuffer.order(ByteOrder.BIG_ENDIAN);
       for (int[] foo : inputData){          
           destByteBuffer.putInt(foo[0]);
           destByteBuffer.putInt(foo[1]);
           destByteBuffer.putInt(0);           
       }          
       return load_buffer(destByteBuffer.array());
   }
   
   /**
    * UNTESTED!
    */
   public IoctlvResult ioctlv(int handle, int cmd,byte[][] inbufs,int[] outbuf_sizes,int[][] ínbufs_ptr,int[][] outbufs_ptr) throws IOException{
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
           
       int[][] iovecs_buffer =  Utils.concatAll(new_inbufs,ínbufs_ptr,outbufs_ptr,new_outbufs);      

       int iovecs = iovec(iovecs_buffer);
       int[] buffer = new int[5];
       buffer[0] = handle;
       buffer[1] = cmd;
       buffer[2] = new_inbufs.length + ínbufs_ptr.length;
       buffer[3] = new_outbufs.length + outbufs_ptr.length;
       buffer[4] = iovecs;       
       int  ret = svc(0x39, buffer);
       
       byte[][] out_data = new byte[new_outbufs.length][];
       i=0;
       for (int[] foo : new_outbufs){
           out_data[i] = read(foo[0],foo[1]);
           i++;
       }
       i=0;
       int[][] free_buffer =  Utils.concatAll(new_inbufs,new_outbufs);      
       for (int[] foo : free_buffer){
           free(foo[0]);
           i++;
       }
       free(iovecs);
       return new IoctlvResult(ret,out_data);       
   }
   
   public int get_fsa_handle() throws IOException{
       if(getFsaHandle()== -1){
           setFsaHandle(open("/dev/fsa", 0));
       }
       return getFsaHandle();
   }
   
   public Result FSA_OpenDir(int handle, String path) throws IOException{
       byte[] inbuffer = new byte[0x520];
       byte[] string = path.getBytes();
       
       System.arraycopy(string , 0, inbuffer, 4, string.length);
       inbuffer[string.length + 4] = 0;
             
       Result res = ioctl(handle, 0x0A, inbuffer, 0x293);
       byte[] data =  res.getData();
       ByteBuffer destByteBuffer = ByteBuffer.allocate(data.length);
       destByteBuffer.order(ByteOrder.BIG_ENDIAN);
       destByteBuffer.put(data);
       
       return new Result(res.getResultValue(),destByteBuffer.getInt(4));
   }
   
   public byte[] intToByteArray(int number){
       ByteBuffer destByteBuffer = ByteBuffer.allocate(4);
       destByteBuffer.order(ByteOrder.BIG_ENDIAN);
       destByteBuffer.putInt(number);
       return destByteBuffer.array();
   }
   
   private ReadDirReturn FSA_ReadDir(int fsa_handle, int dirhandle) throws IOException {
       byte[] inbuffer = new byte[0x520]; 
       System.arraycopy(intToByteArray(dirhandle), 0, inbuffer, 4, 4);       
       Result res = ioctl(fsa_handle, 0x0B, inbuffer, 0x293);
       
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
       if(unknowndata[0] == 0) isFile =true;
       System.arraycopy(data, 0x64, stringData, 0, i);       
       if(res.getResultValue() == 0){
           return new ReadDirReturn(res.getResultValue(),new String(stringData, "UTF-8"),isFile,unknowndata);
       }else{
           return new ReadDirReturn(res.getResultValue());
       }   
   }
   
   public int FSA_Close(int handle) throws IOException{
       int[] buffer = new int[1];
       buffer[0] = handle;
       int result = svc(0x34, buffer);
       if(result == 0){
           setFsaHandle(-1);
       }
       return result;
   }
   
   public int FSA_CloseDir(int handle, int dirhandle) throws IOException{
       byte[] inbuffer = new byte[0x520]; 
       System.arraycopy(intToByteArray(dirhandle), 0, inbuffer, 4, 4);
       Result res = ioctl(handle, 0x0D, inbuffer, 0x293);
       return res.getResultValue();
   }
   
   public List<ReadDirReturn> ls() throws IOException{
       return ls(null,false);
   }
   public List<ReadDirReturn> ls(boolean return_data) throws IOException{
       return ls(null,return_data);
   }
   public List<ReadDirReturn> ls(String targetPath) throws IOException{
       return ls(targetPath,false);
   }
   public List<ReadDirReturn> ls(String targetPath,boolean return_data) throws IOException{
       List<ReadDirReturn> results = new ArrayList<>();
       int fsa_handle = get_fsa_handle();
       String path = targetPath;
       if(targetPath == null || targetPath.isEmpty()){
           path = getCwd();
       }
       
       Result res = FSA_OpenDir(fsa_handle, path);
       if(res.getResultValue() != 0x0){
          System.out.println("opendir error : " + String.format("%08X",res.getResultValue()));
       }
       
       int dirhandle = res.getInt_value();
       while(true){      
           ReadDirReturn result = FSA_ReadDir(fsa_handle, dirhandle);
           if (result.getResult() != 0){
               break;
           }
           if(!return_data){
               if(result.isFile()){
                   System.out.println(result.getFilename());               
               }else{
                   System.out.println(result.getFilename() + "/");
               }
           }else{
               results.add(result);
           }
       }
       int result;
       if((result = FSA_CloseDir(fsa_handle, dirhandle)) != 0){
           System.err.println("CloseDirdone failed!" + result);
       }
       return results;
}
   
    private Socket createSocket(String ip) {
        Socket clientSocket = null;
        try {
            clientSocket = new Socket(ip, 1337);
        } catch (UnknownHostException e) {
            System.err.println("Unkown Host");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO Error Host");
            e.printStackTrace();
        }
        setSocket(clientSocket);
        System.out.println("Connected");
        return clientSocket;        
    }
    
    public void closeSocket(){
        Socket socket = getSocket();
        if(socket!= null){
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }
    public String getIP() {
        return IP;
    }
    public void setIP(String iP) {
        IP = iP;
    }
    private int getFsaHandle() {
        return fsaHandle;
    }
    private void setFsaHandle(int fsaHandle) {
        this.fsaHandle = fsaHandle;
    }
    private String getCwd() {
        return cwd;
    }
    private void setCwd(String cwd) {
        this.cwd = cwd;
    }
    public Socket getSocket() {
        return socket;
    }
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

}
