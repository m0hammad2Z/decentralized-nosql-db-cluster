package org.dbsim.broadcasting.api;

import org.dbsim.broadcasting.transport.SocketClient;

import java.util.concurrent.ConcurrentLinkedQueue;

public interface BroadcastMechanism {
    void broadcast(Message message, Topic topic);

    void setClients(ConcurrentLinkedQueue<SocketClient> clients);
}