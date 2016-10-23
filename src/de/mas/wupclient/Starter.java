package de.mas.wupclient;
import java.io.IOException;
import java.util.List;

import de.mas.wupclient.client.WUPClient;
import de.mas.wupclient.client.operations.FSAOperations;
import de.mas.wupclient.client.operations.UtilOperations;
import de.mas.wupclient.client.utils.FEntry;

public class Starter {
    public static void main(String args[]){        
        WUPClient w = new WUPClient("192.168.0.035");
        try {
            UtilOperations util = UtilOperations.UtilOperationsFactory(w);
            FSAOperations fsa = FSAOperations.FSAOperationsFactory(w);            
            //List<FEntry> result = util.ls("/vol/storage_mlc01/",true);
            printLSRecursive("/vol/storage_mlc01/sys/title/00050010/1004c200/",util);
            w.FSA_Close(w.get_fsa_handle());
            w.closeSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    
    
    public static void printLSRecursive(String path,UtilOperations util) throws IOException{        
        List<FEntry> result = util.ls(path,true);
        for(FEntry entry : result){
            if(entry.isFile()){
                System.out.println(path + entry.getFilename());
            }else{
                String newPath = path + entry.getFilename() + "/";
                System.out.println(newPath);
                printLSRecursive(newPath, util);
            }
        }
    }
}
