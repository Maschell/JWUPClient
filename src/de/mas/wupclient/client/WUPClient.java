package de.mas.wupclient.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import de.mas.wupclient.client.utils.Logger;
import de.mas.wupclient.client.utils.Result;

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
    
    public Result<byte[]> send(int command, byte[] data) throws IOException{
        DataOutputStream outToServer = new DataOutputStream(getSocket().getOutputStream());
        ByteBuffer destByteBuffer = ByteBuffer.allocate(data.length + 4);        
        destByteBuffer.order(ByteOrder.BIG_ENDIAN);
        destByteBuffer.putInt(command);
        destByteBuffer.put(data);
        try {
            outToServer.write(destByteBuffer.array());            
        } catch (IOException e) {
            Logger.logErr("send failed");
            e.printStackTrace();
        }
        destByteBuffer.clear();
        destByteBuffer = ByteBuffer.allocate(0x600);
        byte[] result =  new byte[0x0600];
        int size = getSocket().getInputStream().read(result);
        destByteBuffer.put(result, 0, size);
        int returnValue = destByteBuffer.getInt();
        return new Result<byte[]>(returnValue,Arrays.copyOfRange(result, 4,result.length));        
   }
    
   public byte[] read(int addr, int len) throws IOException{
       ByteBuffer destByteBuffer = ByteBuffer.allocate(8);
       destByteBuffer.order(ByteOrder.BIG_ENDIAN);
       destByteBuffer.putInt(addr);
       destByteBuffer.putInt(len);
       Result<byte[]> result = send(1,destByteBuffer.array());
       if(result.getResultValue() == 0){
           return result.getData();
       }else{
           Logger.logErr("Read error: " + result.getResultValue());
           return null;
       }
   }
   
   public int write(int addr, byte[] data ) throws IOException{       
       ByteBuffer destByteBuffer = ByteBuffer.allocate(4 + data.length);
       destByteBuffer.order(ByteOrder.BIG_ENDIAN);
       destByteBuffer.putInt(addr);
       destByteBuffer.put(data);
       
       Result<byte[]> result = send(0,destByteBuffer.array());
       if(result.getResultValue() == 0){
           return result.getResultValue();
       }else{
           Logger.logErr("write error: " + result.getResultValue());
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
       
       Result<byte[]> result = send(2,destByteBuffer.array());
       if(result.getResultValue() == 0){
           destByteBuffer = ByteBuffer.allocate(0x04);
           destByteBuffer.order(ByteOrder.BIG_ENDIAN);
           destByteBuffer.put(Arrays.copyOfRange(result.getData(), 0, 0x04));
           return destByteBuffer.getInt(0);
       }else{
           Logger.logErr("svc error: " + result.getResultValue());
           return -1;
       }
   }

   public int kill() throws IOException{
       Result<byte[]> result = send(3,new byte[0]);
       return result.getResultValue();
   }
   
   public int memcpy(int dest, int source, int len) throws IOException{
       ByteBuffer destByteBuffer = ByteBuffer.allocate(12);
       destByteBuffer.order(ByteOrder.BIG_ENDIAN);
       destByteBuffer.putInt(dest);
       destByteBuffer.putInt(source);
       destByteBuffer.putInt(len);
       Result<byte[]> result = send(4,destByteBuffer.array());
       if(result.getResultValue() == 0){
           return result.getResultValue();
       }else{
           Logger.logErr("memcpy error: " + result.getResultValue());
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
           int result = svc(0x27, new int[] {0xCAFF,size});
           return result;
       }else{
           int result = svc(0x28, new int[] {0xCAFF,size,align});
           return result;
       }
   }
   public int free(int address) throws IOException{
       if(address == 0){
           return 0;
       }
       return svc(0x29, new int[] {0xCAFF,address});      
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
       int handle = svc(0x33,  new int[] {address,mode});
       free(address);
       return handle;
   }
   public int close(int handle) throws IOException{
       return svc(0x34,new int[]{handle});
   }  
   
   public int get_fsa_handle() throws IOException{
       if(getFsaHandle()== -1){
           setFsaHandle(open("/dev/fsa", 0));
       }
       return getFsaHandle();
   }
   
   public int FSA_Close(int handle) throws IOException{
       int result = svc(0x34, new int[]{handle});
       if(result == 0){
           setFsaHandle(-1);
       }else{
           Logger.logErr("FSA_Close: failed");
       }
       return result;
   }
      
    private Socket createSocket(String ip) {
        Socket clientSocket = null;
        try {
            clientSocket = new Socket(ip, 1337);
        } catch (UnknownHostException e) {
            Logger.logErr("Unkown Host");
            e.printStackTrace();
        } catch (IOException e) {
            Logger.logErr("IO Error Host");
            e.printStackTrace();
        }
        setSocket(clientSocket);
        Logger.log("Connected");
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
    public void setFsaHandle(int fsaHandle) {
        this.fsaHandle = fsaHandle;
    }
    public String getCwd() {
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
