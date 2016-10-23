package de.mas.wupclient;
import java.io.IOException;

import de.mas.wupclient.client.WUPClient;
import de.mas.wupclient.client.operations.DownloadUploadOperations;
import de.mas.wupclient.client.operations.UtilOperations;
import de.mas.wupclient.client.utils.Logger;

public class Starter {
    public static void main(String args[]){
        String ip = "192.168.0.35";
        if(args.length > 0){
            ip = args[0];
        }
        WUPClient w = new WUPClient(ip);
        try {
            UtilOperations util = UtilOperations.UtilOperationsFactory(w);
            DownloadUploadOperations dlul = DownloadUploadOperations.DownloadUploadOperationsFactory(w);
            util.dump_syslog();
            
            Logger.logCmd("Lets into the " + w.getCwd() + "/sys/title/00050010/10040200/" + " folder!");
            util.lsRecursive(w.getCwd() + "/sys/title/00050010/10040200/");
            Logger.logCmd("And download the /code/app.xml to /test/app.xml");
            dlul.downloadFile(w.getCwd() + "/sys/title/00050010/10040200/code", "app.xml", "test", null);
            Logger.logCmd("done!");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                w.FSA_Close(w.get_fsa_handle());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            w.closeSocket();
        }
    }
}
