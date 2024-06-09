package com.dbsim.bootstrapping.model.node;

import com.dbsim.bootstrapping.util.GlobalVar;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class BootstrappingNode {
    private final int id = 0;
    private final String hostname = GlobalVar.BOOTSTRAP_HOSTNAME;
    private final int port = GlobalVar.BOOTSTRAP_PORT;
    private final int broadcastPort = GlobalVar.BROADCASTING_LISTENER_PORT;
    private final Queue<NeighborNode> neighborNodes = new LinkedList<>();


    public int getId() {
        return id;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public void addNeighborNode(NeighborNode neighborNode) {
        if (neighborNode == null || neighborNode.getHostname().isEmpty() || neighborNode.getApiPort() <= 0 || neighborNodes.contains(neighborNode)) {
            return;
        }

        neighborNodes.add(neighborNode);
    }


    public void removeNeighborNode(NeighborNode neighborNode) {
        if (neighborNode == null) {
            return;
        }

        neighborNodes.remove(neighborNode);
    }

    public Queue<NeighborNode> getNeighborNodes() {
        return neighborNodes;
    }

    public NeighborNode getNeighborNode(NeighborNode neighborNode) {
        for (NeighborNode node : neighborNodes) {
            if (node.equals(neighborNode)) {
                return node;
            }
        }
        return null;
    }

    // Round-Robin LoadBalancer
    public NeighborNode getNextNeighborNode() {
        if (neighborNodes.isEmpty()) {
            return null;
        }

        NeighborNode nextNeighborNode = neighborNodes.poll();
        neighborNodes.add(nextNeighborNode);

        return nextNeighborNode;

    }

    // Get Random Quarter of the neighbor nodes along with the last neighbor node added or if the all node if number of nodes is less than 4.
    public List<NeighborNode> getRandomQuarterNeighborNodes() {
        List<NeighborNode> randomNeighborNodes = new ArrayList<>();
        if (neighborNodes.isEmpty()) {
            return randomNeighborNodes;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(0, neighborNodes.size());
        int count = 0;
        for (NeighborNode neighborNode : neighborNodes) {
            if (count == randomIndex) {
                randomNeighborNodes.add(neighborNode);
                break;
            }
            count++;
        }

        return randomNeighborNodes;
    }


}
