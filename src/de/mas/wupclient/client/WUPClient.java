package de.mas.wupclient.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import de.mas.wupclient.client.operations.SystemOperations;
import de.mas.wupclient.client.utils.Logger;
import de.mas.wupclient.client.utils.Result;
import de.mas.wupclient.client.utils.Utils;

public class WUPClient {
    public static final int MAX_READ_SIZE = 0x400;
    private String IP;
    private int fsaHandle = -1;
    private String cwd = "";
    private Socket socket;    

    public WUPClient(String ip){
        setIP(ip);
        createSocket(ip);
        setFSAHandle(-1);
        setCwd("/vol/storage_mlc01");
    }
    
    public Result<byte[]> send(int command, byte[] data) throws IOException{
        DataOutputStream outToServer = new DataOutputStream(getSocket().getOutputStream());
        try {
            outToServer.write(Utils.m_packBE(command,data));            
        } catch (IOException e) {
            Logger.logErr("send failed");
            e.printStackTrace();
        }
        
        byte[] result =  new byte[0x0600];
        int size = getSocket().getInputStream().read(result);
        ByteBuffer destByteBuffer = ByteBuffer.allocate(0x04);        
        destByteBuffer.put(Arrays.copyOfRange(result, 0, 4));
        int returnValue = destByteBuffer.getInt(0);
        return new Result<byte[]>(returnValue,Arrays.copyOfRange(result, 4,size));        
   }
    
   public byte[] read(int addr, int len) throws IOException{
       if(len > WUPClient.MAX_READ_SIZE){
           throw new IOException("read length > " + WUPClient.MAX_READ_SIZE);
       }
       Result<byte[]> result = send(1, Utils.m_packBE(addr,len));
       if(result.getResultValue() == 0){
           return result.getData();
       }else{
           Logger.logErr("Read error: " + result.getResultValue());
           return null;
       }
   }
   
   public int write(int addr, byte[] data ) throws IOException{
       Result<byte[]> result = send(0,Utils.m_packBE(addr,data));
       if(result.getResultValue() == 0){
           return result.getResultValue();
       }else{
           Logger.logErr("write error: " + result.getResultValue());
           return -1;
       }
   }
   
   public int svc(int svc_id, int[] arguments) throws IOException{
       Result<byte[]> result = send(2,Utils.m_packBE(svc_id,arguments));
       if(result.getResultValue() == 0){
           return Utils.bigEndianByteArrayToInt(Arrays.copyOfRange(result.getData(), 0, 0x04));
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
       Result<byte[]> result = send(4, Utils.m_packBE(dest,source,len));
       if(result.getResultValue() == 0){
           return result.getResultValue();
       }else{
           Logger.logErr("memcpy error: " + result.getResultValue());
           return -1;
       }
   }
   
   public int repeatwrite(int dest, int val, int n) throws IOException{
       Result<byte[]> result = send(5, Utils.m_packBE(dest,val,n));
       if(result.getResultValue() == 0){
           return result.getResultValue();
       }else{
           Logger.logErr("repeatwrite error: " + result.getResultValue());
           return -1;
       }
   }
   
   
   public int get_fsa_handle() throws IOException{
       if(fsaHandle == -1){
           setFSAHandle(SystemOperations.SystemOperationsFactory(this).open("/dev/fsa", 0));
       }
       return fsaHandle;
   }
   
   public int FSA_Close(int handle) throws IOException{
       int result = svc(0x34, new int[]{handle});
       if(result == 0){
           setFSAHandle(-1);
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
        Logger.log("Connected to " + ip);
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
    public void setFSAHandle(int fsaHandle) {
        this.fsaHandle = fsaHandle;
    }
    public String getCwd() {
        return cwd;
    }
    public void setCwd(String cwd) {
        this.cwd = cwd;
    }
    public Socket getSocket() {
        return socket;
    }
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

}
