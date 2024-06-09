package org.dbsim.broadcasting.core.bmechanism;

import org.dbsim.broadcasting.api.BroadcastMechanism;
import org.dbsim.broadcasting.api.Message;
import org.dbsim.broadcasting.api.Topic;
import org.dbsim.broadcasting.exception.BroadcastException;
import org.dbsim.broadcasting.transport.SocketClient;
import org.dbsim.broadcasting.util.JsonConverter;

import java.util.concurrent.ConcurrentLinkedQueue;

public class BroadcastToAll implements BroadcastMechanism {
    public BroadcastToAll() {
        super();
    }

    private ConcurrentLinkedQueue<SocketClient> clients;

    @Override
    public void broadcast(Message message, Topic topic) {
        if (clients.isEmpty()) {
            return;
        }

        String json = JsonConverter.convertToJSON(message, topic);
        for (SocketClient client : clients) {
            try {
                client.send(json);
            } catch (Exception e) {
                throw new BroadcastException("Failed to send message to client: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void setClients(ConcurrentLinkedQueue<SocketClient> clients) {
        this.clients = clients;
    }
}
