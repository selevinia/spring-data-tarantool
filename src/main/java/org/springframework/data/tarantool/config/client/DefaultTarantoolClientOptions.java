package org.springframework.data.tarantool.config.client;

import java.util.List;

/**
 * Tarantool connection options holder
 *
 * @author Alexander Rublev
 */
public class DefaultTarantoolClientOptions implements TarantoolClientOptions {
    private List<String> nodes = List.of("localhost:3301");
    private String userName = "guest";
    private String password = "";
    private int connections;
    private int connectTimeout;
    private int readTimeout;
    private int requestTimeout;
    private boolean cluster = true;
    private boolean crudAvailable = true;

    @Override
    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int getConnections() {
        return connections;
    }

    public void setConnections(int connections) {
        this.connections = connections;
    }

    @Override
    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    @Override
    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    @Override
    public boolean isCluster() {
        return cluster;
    }

    public void setCluster(boolean cluster) {
        this.cluster = cluster;
    }

    @Override
    public boolean isCrudAvailable() {
        return crudAvailable;
    }

    public void setCrudAvailable(boolean crudAvailable) {
        this.crudAvailable = crudAvailable;
    }

}
