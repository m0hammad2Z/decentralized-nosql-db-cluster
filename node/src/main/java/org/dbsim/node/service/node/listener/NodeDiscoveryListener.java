package org.dbsim.node.service.node.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dbsim.broadcasting.api.BroadcastListener;
import org.dbsim.broadcasting.api.Message;
import org.dbsim.node.model.node.MainNode;
import org.dbsim.node.model.node.NeighborNode;
import org.dbsim.node.util.SpringContext;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class NodeDiscoveryListener {

    @BroadcastListener(topic = "nodeDiscovery")
    public void nodeDiscovery(Message message) {
        if (message.getContent() instanceof List) {
            processNeighborNodesList((List<Object>) message.getContent());
        }
    }



    private void processNeighborNodesList(List<Object> neighborNodesList) {
        MainNode mainNode = SpringContext.getBean(MainNode.class);
        ObjectMapper objectMapper = SpringContext.getBean(ObjectMapper.class);
        List<Object> neighborNodes = (List<Object>) neighborNodesList;
        for (Object neighborNode : neighborNodes) {
            NeighborNode node = objectMapper.convertValue(neighborNode, NeighborNode.class);

            if(node.hashCode() == mainNode.toNeighborNode().hashCode()) {
                continue;
            }

            if (!mainNode.getNeighborNodes().contains(node)) {
                mainNode.addNeighborNode(node);
            } else {
                mainNode.getNeighborNode(node).setLastHeartbeat(System.currentTimeMillis());
                mainNode.getNeighborNode(node).setLastEventTimestamp(node.getLastEventTimestamp());
            }
        }
    }
}
