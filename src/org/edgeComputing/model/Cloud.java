package org.edgeComputing.model;

public class Cloud extends EdgeEntity {
    private ProxyServer proxyServer;

    public Cloud(){
        super();
    }

    public Cloud(ProxyServer proxyServer){
        super();
        this.proxyServer = proxyServer;
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    public void setProxyServer(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }
}
