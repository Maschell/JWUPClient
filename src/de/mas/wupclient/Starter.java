package de.mas.wupclient;

import java.io.IOException;

public class Starter {
    public static void main(String args[]){        
        WUPClient w = new WUPClient("192.168.0.035");
        try {
            w.ls("/vol/storage_mlc01/");        
            w.FSA_Close(w.get_fsa_handle());
            w.closeSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    
}
