package de.mas.wupclient.client.operations;

import de.mas.wupclient.client.WUPClient;

public abstract class Operations {
    private WUPClient client = null;
    public Operations(WUPClient client){
        setClient(client);
    }
    public WUPClient getClient() {
        return client;
    }
    public void setClient(WUPClient client) {
        this.client = client;
    }
}
