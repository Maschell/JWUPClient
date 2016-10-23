package de.mas.wupclient.client.operations;

import java.util.HashMap;
import java.util.Map;

import de.mas.wupclient.client.WUPClient;

public class MCPOperations extends Operations {   
    private static Map<WUPClient,DownloadUploadOperations> instances = new HashMap<>();
    public static DownloadUploadOperations DownloadUploadOperationsFactory(WUPClient client){
        if(!instances.containsKey(client)){
            instances.put(client, new DownloadUploadOperations(client));
        }
        return instances.get(client);        
    }
    
    SystemOperations system = null;

    public MCPOperations(WUPClient client) {
        super(client);
        setSystem(SystemOperations.SystemOperationsFactory(client));
    }  
    
    public SystemOperations getSystem() {
        return system;
    }

    public void setSystem(SystemOperations system) {
        this.system = system;
    }
}
