package org.dbsim.node.model.node;

import org.dbsim.node.util.GlobalVar;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class MainNode {
    private final int id = toNeighborNode().hashCode();
    private long lastEventTimestamp = 0;
    private final List<NeighborNode> neighborNodes = new LinkedList<>();


    public int getId() {
        return id;
    }

    public long getLastEventTimestamp() {
        return lastEventTimestamp;
    }

    public void setLastEventTimestamp(long lastEventTimestamp) {
        this.lastEventTimestamp = lastEventTimestamp;
    }

    public List<NeighborNode> getNeighborNodes() {
        return neighborNodes;
    }

    public void addNeighborNode(NeighborNode neighborNode) {
        if (neighborNode == null || neighborNode.getHostname().isEmpty() || neighborNode.getApiPort() <= 0) {
            return;
        }

        // Check if the node is already in the neighbor nodes
        if (neighborNodes.contains(neighborNode)) {
            return;
        }

        neighborNodes.add(neighborNode);
    }

    public void removeNeighborNode(NeighborNode neighborNode) {
        if (neighborNode == null) {
            return;
        }


        // Check if the node is in the neighbor nodes
        if (neighborNodes.contains(neighborNode)) {
            neighborNodes.remove(neighborNode);
        }
    }

    public NeighborNode getNeighborNode(NeighborNode neighborNode) {
        if (neighborNode == null) {
            return null;
        }

        for (NeighborNode node : neighborNodes) {
            if (node.equals(neighborNode)) {
                return node;
            }
        }

        return null;
    }

    public NeighborNode getNeighborNode(int hashId) {
        for (NeighborNode node : neighborNodes) {
            if (node.hashCode() == hashId) {
                return node;
            }
        }

        return null;
    }

    public NeighborNode getNeighborNode(String hostname, int apiPort) {
        for (NeighborNode node : neighborNodes) {
            if (node.getHostname().equals(hostname) && node.getApiPort() == apiPort) {
                return node;
            }
        }

        return null;
    }

    public NeighborNode getNeighborNodeByIndex(int index) {
        if (index < 0 || index >= neighborNodes.size()) {
            return null;
        }

        return neighborNodes.get(index);
    }


    // Get a random quarter of the neighbor nodes to broadcast the information about to the network
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
            }
            count++;
        }

        return randomNeighborNodes;
    }

    // Get Affinity Node id (Get the closest node to the document id)
    public int getAffinityNodeId(String str) {
        if (str == null) return -1;

        int hash = Objects.hash(str);
        int closestNodeId = -1;
        int closestDistance = Integer.MAX_VALUE;

        for (NeighborNode node : neighborNodes) {
            int distance = Math.abs(node.hashCode() - hash);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestNodeId = node.hashCode();
            }
        }

        int distance = Math.abs(toNeighborNode().hashCode() - hash);
        if (distance < closestDistance) {
            closestNodeId = toNeighborNode().hashCode();
        }

        return closestNodeId;
    }


    public NeighborNode toNeighborNode() {
        NeighborNode node = new NeighborNode(GlobalVar.HOSTNAME, GlobalVar.PORT, GlobalVar.BROADCASTING_LISTENER_PORT,  System.currentTimeMillis());
        node.setLastEventTimestamp(lastEventTimestamp);
        return node;
    }


}
