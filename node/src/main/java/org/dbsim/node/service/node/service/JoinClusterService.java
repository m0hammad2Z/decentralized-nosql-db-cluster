package org.dbsim.node.service.node.service;

import org.dbsim.broadcasting.api.Message;
import org.dbsim.broadcasting.api.Topic;
import org.dbsim.broadcasting.core.Broadcaster;
import org.dbsim.node.model.node.MainNode;
import org.dbsim.node.util.GlobalVar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class JoinClusterService {
    private MainNode mainNode;
    private Broadcaster broadcaster;
    private final Topic joinTopic = new Topic("join");

    @Autowired
    public JoinClusterService(MainNode mainNode, Broadcaster broadcaster) {
        this.mainNode = mainNode;
        this.broadcaster = broadcaster;
    }


    @Scheduled(fixedRate = 10000)
    public void joinPoint() {
        Map<String, Object> map = Map.of("neighborNode", mainNode.toNeighborNode(), "nodeId", mainNode.getId());
        Message message = new Message(map);
        broadcaster.broadcastToAddress(message, joinTopic, GlobalVar.BOOTSTRAPPING_PORT, GlobalVar.BOOTSTRAPPING_SERVER);
    }
}