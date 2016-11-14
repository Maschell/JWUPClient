package de.mas.wupclient;
import java.io.IOException;
import java.util.Scanner;

import de.mas.wupclient.client.WUPClient;
import de.mas.wupclient.client.operations.DownloadUploadOperations;
import de.mas.wupclient.client.operations.DumperOperations;
import de.mas.wupclient.client.operations.UtilOperations;

public class Starter {
    public static void main(String args[]){
        String ip = "192.168.0.35";
        if(args.length > 0){
            ip = args[0];
        }
        WUPClient w = new WUPClient(ip);
        try {            
            boolean exit = false;
        
            System.out.println("JWUPClient 0.1a");
            System.out.println("Please enter a command. Enter \"exit\" to exit.");
            System.out.println();
            System.out.print(w.getCwd() + " > ");
            Scanner reader = new Scanner(System.in);  // Reading from System.in

            while(!exit){
                
                String input = reader.nextLine();
                if(input.equals("exit")){
                    exit = true;
                   
                    break;
                }
                processCommand(input,w);
                System.out.println();
                System.out.print(w.getCwd() + " > ");               
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                w.FSA_Close(w.get_fsa_handle());
            } catch (IOException e) {             
            }
            w.closeSocket();
        }
    }

    private static void processCommand(String input,WUPClient w) throws IOException {
        if(input == null || input.isEmpty()){
            return;
        }
        UtilOperations util = UtilOperations.UtilOperationsFactory(w);
        DownloadUploadOperations dlul = DownloadUploadOperations.DownloadUploadOperationsFactory(w);
        DumperOperations dump = DumperOperations.DumperOperationsFactory(w);
       
        String[] inputs = input.split(" ");
        switch(inputs[0]){
            case "ls":
                if(inputs.length > 1){
                    util.ls(inputs[1]);
                }else{
                    util.ls();
                }
                break;
            case "lsr":
                util.lsRecursive();
                break;
            case "sysdump":
                util.dump_syslog();
                break;
            case "cd":
                if(inputs.length > 1){
                    util.cd(inputs[1]);
                }else{
                    util.cd();
                }
                
                break;
            case "dldir":
                String destination = null;
                String source = w.getCwd();
                boolean fullpath = false;
                if(inputs.length > 1){                    
                    for(int i = 1;i < inputs.length;i++){
                        if(inputs[i].equals("-dst")){
                            if(inputs.length >= i+1){
                                destination = inputs[i+1];
                                i++;
                            }
                        }else if(inputs[i].equals("-src")){
                            if(inputs.length >= i+1){
                                source = inputs[i+1];
                                i++;
                            }
                        }else if(inputs[i].equals("-fullpath")){
                            fullpath = true;                           
                        }                        
                    }
                }
                dlul.downloadFolder(source,destination,fullpath);
                
                break;
            case "dl":
                if(inputs.length == 2){
                    dlul.downloadFile("", inputs[1]);
                }else if(inputs.length == 3){
                    dlul.downloadFile("", inputs[1], inputs[2]);
                }
                
                break;
            case "dlfp": //download to full path
                if(inputs.length == 2){
                    dlul.downloadFile("", inputs[1],w.getCwd());
                }else if(inputs.length == 3){
                    dlul.downloadFile("", inputs[1],inputs[2] + "/" + w.getCwd());
                }
                
                break;
            case "dumpdisc":
                String pattern = ".*";
                boolean deepSearch = false;
                if(inputs.length > 1){                    
                    for(int i = 1;i < inputs.length;i++){
                        if(inputs[i].equals("-file")){
                            if(inputs.length >= i+1){
                                pattern = inputs[i+1];
                                i++;
                            }
                        }
                        if(inputs[i].equals("-deepSearch")){
                            deepSearch = true;
                        }
                    }
                }
                dump.dumpDisc(pattern,deepSearch);
                break;
            default:
                System.out.println("Command not found!");
                break;
        }
        
    }
}
