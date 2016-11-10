package de.mas.wupclient.client.utils;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class FStats {   
    int[] unk1 = new int[0x4];
    int size; // size in bytes
    int physsize; // physical size on disk in bytes
    int[] unk2 = new int[0x13];
    
    public FStats(byte[] data) {
        if(data == null || data.length < 0x64){
            return;
        }
        ByteBuffer buffer = ByteBuffer.allocate(0x64);
        buffer.put(Arrays.copyOfRange(data, 0x00, 0x64));
        int offset = 0x00;
        for(int i = 0;i<unk1.length;i++){
            unk1[i] = buffer.getInt(offset);
            offset +=0x04;
        }
        
        this.size = buffer.getInt(offset);;
        offset += 4;
        this.physsize = buffer.getInt(offset);;
        offset += 4;
        for(int i = 0;i<unk2.length;i++){
            unk2[i] = buffer.getInt(offset);
            offset +=0x04;
        }
    }
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        return sb.append("Size: " + size + " Physicalsize:" + physsize).toString();
    }
        
}
