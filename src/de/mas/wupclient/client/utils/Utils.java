package de.mas.wupclient.client.utils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

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
        while(data[i] != 0 && (i) < data.length){
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
        ByteBuffer destByteBuffer = ByteBuffer.allocate(4);
        destByteBuffer.order(ByteOrder.BIG_ENDIAN);
        destByteBuffer.putInt(value);
        return destByteBuffer.array();
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
    
}
