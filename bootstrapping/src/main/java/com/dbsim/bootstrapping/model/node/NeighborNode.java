package com.dbsim.bootstrapping.model.node;

import java.util.Objects;
public class NeighborNode {
    private final String hostname;
    private final int apiPort;
    private final int listenerPort;
    private long lastHeartbeat;

    private long lastEventTimestamp = 0;

    public NeighborNode(){
        this.hostname = "";
        this.apiPort = 0;
        this.listenerPort = 0;
        lastHeartbeat = System.currentTimeMillis();
    }
    public NeighborNode(String hostname, int apiPort, int listenerPort, long lastHeartbeat) {
        this.hostname = hostname;
        this.apiPort = apiPort;
        this.listenerPort = listenerPort;
        this.lastHeartbeat = lastHeartbeat;
    }


    public String getHostname() {
        return hostname;
    }

    public int getApiPort() {
        return apiPort;
    }

    public int getListenerPort() {
        return listenerPort;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }
    public long getLastEventTimestamp() {
        return lastEventTimestamp;
    }

    public void setLastEventTimestamp(long lastEventTimestamp) {
        this.lastEventTimestamp = lastEventTimestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NeighborNode neighborNode)) {
            return false;
        }

        return hostname.equals(neighborNode.hostname) && apiPort == neighborNode.apiPort;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostname, apiPort);
    }

    @Override
    public String toString() {
        return "{"+
                "hostname='" + hostname +
                ", apiPort=" + apiPort +
                ", listenerPort=" + listenerPort +
                ", lastEventTimestamp=" + lastEventTimestamp +
                '}';
    }
}
