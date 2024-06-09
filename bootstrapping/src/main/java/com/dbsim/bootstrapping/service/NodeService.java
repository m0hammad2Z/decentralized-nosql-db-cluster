package com.dbsim.bootstrapping.service;

import com.dbsim.bootstrapping.model.node.BootstrappingNode;
import com.dbsim.bootstrapping.model.node.NeighborNode;
import com.dbsim.bootstrapping.util.SpringContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dbsim.broadcasting.api.BroadcastListener;
import org.dbsim.broadcasting.api.Message;
import org.dbsim.broadcasting.api.Topic;
import org.dbsim.broadcasting.core.Broadcaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class NodeService {

    @Autowired
    private Broadcaster broadcaster;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Topic nodeDiscoveryTopic = new Topic("nodeDiscovery");

    @Scheduled(fixedRate = 5000)
    public void broadcastNodes() {
        BootstrappingNode bootstrappingNode = SpringContext.getBean(BootstrappingNode.class);

        Set<NeighborNode> neighborNodes = new HashSet<>(bootstrappingNode.getNeighborNodes());
        for (NeighborNode neighborNode : neighborNodes) {
            broadcaster.addClient(neighborNode.getHostname(), neighborNode.getListenerPort());
            // Remove nodes that have not sent a heartbeat in the last 50 seconds
            if (System.currentTimeMillis() - neighborNode.getLastHeartbeat() > 50000) {
                bootstrappingNode.removeNeighborNode(neighborNode);
                broadcaster.removeClient(neighborNode.getHostname(), neighborNode.getListenerPort());
                System.out.println("Removed node " + neighborNode.getHostname() + ":" + neighborNode.getListenerPort());
            }
        }
        broadcaster.broadcastRandom(new Message<>(bootstrappingNode.getRandomQuarterNeighborNodes()), nodeDiscoveryTopic, 5);

        System.out.println(bootstrappingNode.getNeighborNodes());
    }

    @BroadcastListener(topic = "join")
    public void nodeJoinPoint(Message message) {
        if (message.getContent() instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) message.getContent();
            NeighborNode neighborNode = objectMapper.convertValue(map.get("neighborNode"), NeighborNode.class);

            BootstrappingNode bootstrappingNode = SpringContext.getBean(BootstrappingNode.class);
            if (!bootstrappingNode.getNeighborNodes().contains(neighborNode)) {
                bootstrappingNode.addNeighborNode(neighborNode);
            } else {
                bootstrappingNode.getNeighborNode(neighborNode).setLastHeartbeat(System.currentTimeMillis());
                bootstrappingNode.getNeighborNode(neighborNode).setLastEventTimestamp((neighborNode.getLastEventTimestamp()));
            }
        }
    }
}