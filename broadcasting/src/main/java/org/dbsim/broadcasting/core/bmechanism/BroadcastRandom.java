package org.dbsim.broadcasting.core.bmechanism;

import org.dbsim.broadcasting.api.BroadcastMechanism;
import org.dbsim.broadcasting.api.Message;
import org.dbsim.broadcasting.api.Topic;
import org.dbsim.broadcasting.exception.BroadcastException;
import org.dbsim.broadcasting.transport.SocketClient;
import org.dbsim.broadcasting.util.JsonConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BroadcastRandom implements BroadcastMechanism {
    private int x;

    public BroadcastRandom(){
        super();
    }
    public BroadcastRandom(int x){
        this.x = x;
    }

    private ConcurrentLinkedQueue<SocketClient> clients;

    @Override
    public void broadcast(Message message, Topic topic) {
        if (clients.isEmpty()) {
            return;
        }

        String json = JsonConverter.convertToJSON(message, topic);

        // Prepare a list to hold the selected clients
        List<SocketClient> selectedClients = new ArrayList<>(x);

        // Iterate over the clients queue
        for (SocketClient client : clients) {
            // If we have enough clients, break the loop
            if (selectedClients.size() >= x) {
                break;
            }

            // Add the client to the selected clients with a certain probability
            if (Math.random() < (double) x / clients.size()) {
                selectedClients.add(client);
            }
        }

        // Send the message to the selected clients
        for (SocketClient client : selectedClients) {
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

    public void setX(int x) {
        this.x = x;
    }
}
