package org.dbsim.node.service.node.service;

import org.dbsim.broadcasting.api.Message;
import org.dbsim.broadcasting.api.Topic;
import org.dbsim.broadcasting.core.Broadcaster;
import org.dbsim.node.model.node.MainNode;
import org.dbsim.node.model.node.NeighborNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NeighborNodesGossipService {
    @Autowired
    private MainNode mainNode;
    @Autowired
    private Broadcaster broadcaster;
    private final Topic nodeDiscovery = new Topic("nodeDiscovery");


    // Broadcast information about the neighbor nodes to the network
    @Scheduled(fixedRate = 5000)
    public void reBroadcast() {
        addClients();
        List<NeighborNode> neighborNodes = mainNode.getRandomQuarterNeighborNodes();
        neighborNodes.add(mainNode.toNeighborNode());
        Message<List<NeighborNode>> message = new Message<>(neighborNodes);
        broadcaster.broadcastRandom(message, nodeDiscovery, 5);
    }

    // Add the non-main nodes as clients
    private void addClients() {
        for (NeighborNode neighborNode : mainNode.getNeighborNodes()) {
            broadcaster.addClient(neighborNode.getHostname(), neighborNode.getListenerPort());
        }
    }
}