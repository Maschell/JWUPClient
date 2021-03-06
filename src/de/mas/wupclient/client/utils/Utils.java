package de.mas.wupclient.client.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Utils {
    public static String ByteArrayToString(byte[] ba)
    {
      if(ba == null) return null;
      StringBuilder hex = new StringBuilder(ba.length * 2);
      for(byte b : ba){
        hex.append(String.format("%02X", b));
      }
      return hex.toString();
    }
    
    @SafeVarargs
    public static <T> T[] concatAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
          totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
          System.arraycopy(array, 0, result, offset, array.length);
          offset += array.length;
        }
        return result;
    }
    
    public static String getStringFromByteArray(byte[] data){
        int i = 0;
        while(data[i] != 0 && (i) < data.length-1){
            i++;
        }
        String string = "";
        try {
            string = new String(Arrays.copyOf(data, i),"UTF-8");
        } catch (UnsupportedEncodingException e) {
           Logger.logErr("UnsupportedEncodingException - couldn't creat String from byte[]");
        }
        return string;
    }

    public static void writeIntToByteArray(byte[] target, int dirhandle, int offset) {
        if(target != null && target.length >= (offset + 0x04)){
            System.arraycopy(intToBigEndianByteArray(dirhandle), 0, target, offset, 4);
        }else{
            Logger.logErr("writeIntToByteArray failed. Not enough space in targetBuffer");
        }
    }
    
    public static byte[] intToBigEndianByteArray(int value){
        return m_packBE(value);
    }
    
    public static int bigEndianByteArrayToInt(byte[] data){
        return bigEndianByteArrayToInt(data,0);
    }
    public static int bigEndianByteArrayToInt(byte[] data,int offset){
        if(data == null){
            Logger.logErr("bigEndianByteArrayToInt failed. data is null");
            return 0;
        }
        ByteBuffer destByteBuffer = ByteBuffer.allocate(data.length);
        destByteBuffer.order(ByteOrder.BIG_ENDIAN);
        destByteBuffer.put(data);
        return destByteBuffer.getInt(offset);
    }

    public static boolean writeNullTerminatedStringToByteArray(byte[] target, String input, int offset) {        
        if(writeStringToByteArray(target, input, offset)){
            int nullTerminatorOffset = offset + input.getBytes().length;
            if(target != null && target.length >= (nullTerminatorOffset + 1)){
                target[nullTerminatorOffset] = 0;
                return true;
            }else{
                Logger.logErr("writeNullTerminatedStringToByteArray failed. Not enough space in targetBuffer");
            }
        }
        return false;
    }
    
    public static boolean writeStringToByteArray(byte[] target, String input, int offset) {
        byte[] stringBytes = input.getBytes();
        if(target != null && target.length >= (offset + stringBytes.length)){
            System.arraycopy(stringBytes , 0, target, offset, stringBytes.length);
            return true;
        }else{
            Logger.logErr("writeStringToByteArray failed. Not enough space in targetBuffer");
        }
        return false;
    }
    
    public static long StringToLong(String s) {
        try{
            BigInteger bi = new BigInteger(s, 16);          
            return bi.longValue();
        }catch(NumberFormatException e){
            System.err.println("Invalid Title ID");
            return 0L;
        }
    }
    
    public static MetaInformation readMeta(InputStream bis) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        
        String ID6 = null;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(bis);
             String proc = document.getElementsByTagName("product_code").item(0).getTextContent().toString();
             String comp = document.getElementsByTagName("company_code").item(0).getTextContent().toString();
             String title_id = document.getElementsByTagName("title_id").item(0).getTextContent().toString();
             
             String longname = document.getElementsByTagName("longname_en").item(0).getTextContent().toString();
             longname = longname.replace("\n", " ");
             String id = proc.substring(proc.length()-4, proc.length());
             comp = comp.substring(comp.length()-2, comp.length());
             ID6 = id+comp;
             String  company_code = document.getElementsByTagName("company_code").item(0).getTextContent().toString();
             String content_platform = document.getElementsByTagName("content_platform").item(0).getTextContent().toString();
             String region = document.getElementsByTagName("region").item(0).getTextContent().toString();
             MetaInformation nusinfo = new MetaInformation(Utils.StringToLong(title_id),longname,ID6,proc,content_platform,company_code,(int) StringToLong(region));
             return nusinfo;
            
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Logger.log("Error while parsing the meta files");
        }
        return null;
    }
    
    public static String getParentDir(String path){
        if(path == null || path.isEmpty() || path.equals("/")){
            return "/";
        }
        String new_path = "";
        String [] pathpath = path.split("/");
        for(int i = 0;i<pathpath.length-1;i++){
            if(!pathpath[i].equals("")){                
                new_path += "/" + pathpath[i] ;
            }
        }
        return new_path;
        
    }
    
    public static void createSubfolder(String folder){
        if(folder.endsWith("/")) folder += "_";
        String [] path = folder.split("/");     
        File folder_ = null;
        String foldername = new String();
        if(path.length == 1){
            folder_ = new File(folder);             
            if(!folder_.exists()){
                folder_.mkdir();                    
            }
        }
        for(int i = 0;i<path.length-1;i++){
            if(!path[i].equals("")){                
                foldername += path[i] + "/";
                folder_ = new File(foldername);             
                if(!folder_.exists()){
                    folder_.mkdir();                    
                }
            }           
        }
    }
    
    public static byte[] m_packBE(Object... args){
        return m_pack(ByteOrder.BIG_ENDIAN,args);
     }
     
     public static byte[] m_pack(ByteOrder order,Object... args){
         int totalLength = 0;
         for(Object o :args){
             if(o instanceof String){
                 totalLength += ((String)o).getBytes().length;
             }else if(o instanceof Integer){
                 totalLength += 0x04;
             }else if(o instanceof int[]){
                 totalLength += 0x04 * ((int[])o).length;
             }else if(o instanceof byte[]){
                 totalLength += ((byte[])o).length;
             }else{
                 Logger.logErr("Can't use this datatype");
             }
         }
         ByteBuffer destByteBuffer = ByteBuffer.allocate(totalLength);
         destByteBuffer.order(order);
         for(Object o :args){
             if(o instanceof String){
                 destByteBuffer.put(((String)o).getBytes());
             }else if(o instanceof Integer){
                 destByteBuffer.putInt((Integer)o);
             }else if(o instanceof int[]){
                 for(Integer i : ((int[])o)){
                     destByteBuffer.putInt(i);
                 }
             }else if(o instanceof byte[]){
                 destByteBuffer.put((byte[])o);
             }else{
                 Logger.logErr("Can't use this datatype");
             }
         }
         return destByteBuffer.array();
     }
    
}
