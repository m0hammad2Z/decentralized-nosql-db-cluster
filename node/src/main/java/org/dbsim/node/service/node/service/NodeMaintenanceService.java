package org.dbsim.node.service.node.service;

import org.dbsim.broadcasting.core.Broadcaster;
import org.dbsim.node.model.node.MainNode;
import org.dbsim.node.model.node.NeighborNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NodeMaintenanceService {

    private Broadcaster broadcaster;

    private MainNode mainNode;

    @Autowired
    public NodeMaintenanceService(Broadcaster broadcaster, MainNode mainNode) {
        this.broadcaster = broadcaster;
        this.mainNode = mainNode;
    }

    @Scheduled(fixedRate = 10000)
    private void removeNodes() {
        List<NeighborNode> neighborNodes = mainNode.getNeighborNodes();
        for (NeighborNode neighborNode : neighborNodes) {
            if (System.currentTimeMillis() - neighborNode.getLastHeartbeat() > 40000) {
                mainNode.removeNeighborNode(neighborNode);
                broadcaster.removeClient(neighborNode.getHostname(), neighborNode.getListenerPort());
            }
        }

    }

}
