package org.dbsim.node.service.node.service;

import org.dbsim.broadcasting.api.Message;
import org.dbsim.broadcasting.api.Topic;
import org.dbsim.broadcasting.core.Broadcaster;
import org.dbsim.node.enums.DBEventType;
import org.dbsim.node.model.event.DBEvent;
import org.dbsim.node.model.node.MainNode;
import org.dbsim.node.model.node.NeighborNode;
import org.dbsim.node.service.node.service.node_selection.CustomeGossipStrategy;
import org.dbsim.node.service.node.service.node_selection.NodeSelectionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;

@Service
public class DBGossipService {

    @Autowired
    private Broadcaster broadcaster;
    @Autowired
    private MainNode mainNode;
    @Autowired
    private EventLogService eventLogService;
    private final Queue<DBEvent> eventQueue = new ConcurrentLinkedQueue<>();
    private final Map<Integer, Long> lastGossipTimestampMap = new ConcurrentHashMap<>(); // Last gossip timestamp from each node
    private final Topic dbEventTopic = new Topic("db-event");
    private final Topic missingEventTopic = new Topic("missing-events-request");
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    @PostConstruct
    public void init() {
        executorService.scheduleAtFixedRate(this::broadcastEvents, 0, 2, TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(this::checkMissingEvents, 0, 10, TimeUnit.SECONDS);
    }

    public synchronized void addEvent(DBEvent event) {
        if(event.getNodeID() == mainNode.toNeighborNode().hashCode() || event.getTimestamp() < lastGossipTimestampMap.getOrDefault(event.getNodeID(), 0L) || eventQueue.contains(event) || event.getGossipCount() <= 0){
            return;
        }
        eventQueue.add(event);
        event.decrementGossipCount();
        System.out.println(event + " " + event.getGossipCount());
        lastGossipTimestampMap.put(event.getNodeID(), event.getTimestamp());
    }

    public void addEvent(DBEventType type, Object data) {
        DBEvent event = new DBEvent(type, data, 2, mainNode.toNeighborNode().hashCode());
        eventQueue.add(event);
        eventLogService.addEvent(event);
        mainNode.setLastEventTimestamp(event.getTimestamp());
    }

    public void broadcastEvents(){
        if(eventQueue.isEmpty()) return;

        List<DBEvent> events = new ArrayList<>();
        while(!eventQueue.isEmpty()){
            events.add(eventQueue.poll());
        }

        Message<List<DBEvent>> message = new Message<>(events);
        broadcaster.broadcastToAll(message, dbEventTopic);

        for(NeighborNode neighborNode : mainNode.getNeighborNodes()){
            lastGossipTimestampMap.put(neighborNode.hashCode(), System.currentTimeMillis());
        }
    }


    public void checkMissingEvents(){
        for(NeighborNode neighborNode : mainNode.getNeighborNodes()){
            Long lastGossipTimestamp = lastGossipTimestampMap.getOrDefault(neighborNode.hashCode(), 0L);
            Integer nodeID = mainNode.toNeighborNode().hashCode();
            if(lastGossipTimestampMap.getOrDefault(neighborNode.hashCode(), 0L) < neighborNode.getLastEventTimestamp()){
                Message<Map<String, Object>> message = new Message<>(new HashMap<>());
                message.getContent().put("nodeID", nodeID);
                message.getContent().put("timestamp", lastGossipTimestamp);
                broadcaster.broadcastToAddress(message, missingEventTopic, neighborNode.getListenerPort(), neighborNode.getHostname());
            }
        }
    }

    public void broadcastEventsSince(int nodeID, long timestamp){
        List<DBEvent> events = eventLogService.getEventsSince(timestamp);
        if (events.isEmpty()) return;

        Message<List<DBEvent>> message = new Message<>(events);
        NeighborNode neighborNode = mainNode.getNeighborNode(nodeID);
        broadcaster.broadcastToAddress(message, dbEventTopic, neighborNode.getListenerPort(), neighborNode.getHostname());

        lastGossipTimestampMap.put(nodeID, System.currentTimeMillis());
    }

    @PreDestroy
    public void destroy() {
        executorService.shutdown();
    }
}