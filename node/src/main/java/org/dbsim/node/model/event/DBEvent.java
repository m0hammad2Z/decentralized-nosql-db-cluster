package org.dbsim.node.model.event;

import org.dbsim.node.enums.DBEventType;

import java.util.Objects;

public class DBEvent implements Comparable<DBEvent> {
    private DBEventType type;
    private Object data;
    private int gossipCount = 3;
    private int nodeID;
    private long timestamp = System.currentTimeMillis();

    public DBEvent() {
    }

    public DBEvent(DBEventType type, Object data, int gossipCount, int nodeID) {
        this.type = type;
        this.data = data;
        this.gossipCount = gossipCount;
        this.nodeID = nodeID;
    }

    public DBEventType getType() {
        return type;
    }

    public void setType(DBEventType type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getGossipCount() {
        return gossipCount;
    }

    public void decrementGossipCount() {
        this.gossipCount--;
    }

    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBEvent dbEvent = (DBEvent) o;
        return gossipCount == dbEvent.gossipCount && nodeID == dbEvent.nodeID && timestamp == dbEvent.timestamp && type == dbEvent.type && Objects.equals(data, dbEvent.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, data, gossipCount, nodeID, timestamp);
    }

    @Override
    public int compareTo(DBEvent o) {
        return Long.compare(this.timestamp, o.timestamp);
    }
}