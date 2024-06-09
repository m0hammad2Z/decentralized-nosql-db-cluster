package org.dbsim.broadcasting.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbsim.broadcasting.api.BroadcastMechanism;
import org.dbsim.broadcasting.api.Message;
import org.dbsim.broadcasting.api.Topic;
import org.dbsim.broadcasting.core.bmechanism.BroadcastRandom;
import org.dbsim.broadcasting.core.bmechanism.BroadcastToAddress;
import org.dbsim.broadcasting.core.bmechanism.BroadcastToAll;
import org.dbsim.broadcasting.transport.SocketClient;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Broadcaster {
    protected static final Logger logger = LogManager.getLogger(Broadcaster.class);
    protected ConcurrentLinkedQueue<SocketClient> clients;
    private final ExecutorService executorService;
    private final BroadcastMechanism broadcastToAll;
    private final BroadcastMechanism broadcastRandom;
    private final BroadcastMechanism broadcastToAddress;
    private  final int batchSize;


    public Broadcaster(int batchSize) {
        this.batchSize = batchSize;
        this.clients = new ConcurrentLinkedQueue<>();
        this.executorService = Executors.newCachedThreadPool();
        this.broadcastToAll = new BroadcastToAll();
        this.broadcastRandom = new BroadcastRandom();
        this.broadcastToAddress = new BroadcastToAddress();
        broadcastToAll.setClients(clients);
        broadcastRandom.setClients(clients);
        broadcastToAddress.setClients(clients);
    }

    public final void broadcastToAll(Message message, Topic topic) {
        broadcastToAll.broadcast(message, topic);
    }

    public final void broadcastRandom(Message message, Topic topic, int max) {
        ((BroadcastRandom) broadcastRandom).setX(max);
        broadcastRandom.broadcast(message, topic);
    }

    public final void broadcastToAddress(Message message, Topic topic, int port, String host) {
        ((BroadcastToAddress) broadcastToAddress).setPort(port);
        ((BroadcastToAddress) broadcastToAddress).setHost(host);
        broadcastToAddress.broadcast(message, topic);
    }

    public final void broadcaster(BroadcastMechanism broadcastMechanism, Message message, Topic topic) {
        broadcastMechanism.broadcast(message, topic);
    }

    public final void addClient(String host, int port) {
        executorService.submit(() -> {
            for (SocketClient client : clients) {
                if (client.equals(port, host)) {
                    return;
                }
            }
            SocketClient client = new SocketClient(host, port, batchSize);
            clients.add(client);
        });
    }

    public final void removeClient(String host, int port) {
        executorService.submit(() -> {
            clients.removeIf(client -> client.equals(port, host));
        });
    }

    public final ConcurrentLinkedQueue<SocketClient> getClients() {
        return clients;
    }

    public void setClients(ConcurrentLinkedQueue<SocketClient> clients) {
        this.clients = clients;
    }

}