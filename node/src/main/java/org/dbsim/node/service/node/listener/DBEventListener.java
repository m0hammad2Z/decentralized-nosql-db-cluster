package org.dbsim.node.service.node.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.dbsim.node.dto.db.CollectionDTO;
import org.dbsim.node.dto.db.DatabaseDTO;
import org.dbsim.node.dto.db.DocumentDTO;
import org.dbsim.node.dto.db.IndexDTO;
import org.dbsim.node.dto.user.UserDTO;
import org.dbsim.node.exception.DBOperationException;
import org.dbsim.node.service.node.service.DBGossipService;
import org.dbsim.node.util.SpringContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.dbsim.broadcasting.api.BroadcastListener;
import org.dbsim.broadcasting.api.Message;
import org.dbsim.node.model.event.DBEvent;
import org.dbsim.node.service.db.DatabaseService;
import org.dbsim.node.service.db.CollectionService;
import org.dbsim.node.service.db.DocumentService;
import org.dbsim.node.service.db.IndexService;
import org.dbsim.node.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


@Component
public class DBEventListener {

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private IndexService indexService;

    @Autowired
    private UserService userService;


    private final static Queue<DBEvent> eventQueue = new ConcurrentLinkedQueue<>();
    private final static Logger LOGGER = LoggerFactory.getLogger(DBEventListener.class);


    // This method is used to add new events created by other nodes (To be processed by this node)
    @BroadcastListener(topic = "db-event")
    public void onEvent(Message message) {
        ObjectMapper objectMapper = SpringContext.getBean(ObjectMapper.class);
        Object content = message.getContent();
        if (content instanceof List) {
            List<Object> events = (List<Object>) content;
            for (Object event : events) {
                DBEvent dbEvent = objectMapper.convertValue(event, DBEvent.class);
                eventQueue.add(dbEvent);
            }
        } else if (content instanceof Map) {
            DBEvent dbEvent = objectMapper.convertValue(content, DBEvent.class);
            eventQueue.add(dbEvent);
        }
    }

    // To make sure that the node is up-to-date with the latest events
    @BroadcastListener(topic = "missing-events-request")
    public void onRequest(Message<Map<String, Object>> message) {
        ObjectMapper objectMapper = SpringContext.getBean(ObjectMapper.class);
        DBGossipService gossipService = SpringContext.getBean(DBGossipService.class);
        Map<String, Object> data = objectMapper.convertValue(message.getContent(), Map.class);
        int nodeID = (int) data.get("nodeID");
        Object timestampObj = data.get("timestamp");

        if (timestampObj instanceof Long) {
            long lastTimestamp = (long) timestampObj;
            gossipService.broadcastEventsSince(nodeID, lastTimestamp);
        } else if (timestampObj instanceof Integer) {
            long lastTimestamp = ((Integer) timestampObj).longValue();
            gossipService.broadcastEventsSince(nodeID, lastTimestamp);
        }
    }


    // This method is used to process the events in the event queue
    @Scheduled(fixedRate = 3000)
    public void processEvents() {
        while(!eventQueue.isEmpty()) {
            DBEvent event = eventQueue.poll();
            processEvent(event);
        }
    }

    // This method is used to process a single event
    private void processEvent(DBEvent event) {
        ObjectMapper objectMapper = SpringContext.getBean(ObjectMapper.class);
        DBGossipService gossipService = SpringContext.getBean(DBGossipService.class);
        Object eventData = event.getData();
        try {
            switch (event.getType()) {
                case DATABASE_CREATED -> databaseService.saveDatabase(objectMapper.convertValue(eventData, DatabaseDTO.class));
                case DATABASE_DELETED -> databaseService.deleteDatabase(objectMapper.convertValue(eventData, String.class));
                case COLLECTION_CREATED -> collectionService.saveCollection(objectMapper.convertValue(eventData, CollectionDTO.class));
                case COLLECTION_DELETED -> {
                    Map<String, Object> dataMap = objectMapper.convertValue(eventData, Map.class);
                    collectionService.deleteCollection(dataMap.get("collectionName").toString(), dataMap.get("databaseName").toString());
                }
                case DOCUMENT_CREATED -> documentService.saveDocumentHasId(objectMapper.convertValue(eventData, DocumentDTO.class));
                case DOCUMENT_UPDATED -> documentService.updateDocument(objectMapper.convertValue(eventData, DocumentDTO.class));
                case DOCUMENT_DELETED -> {
                    Map<String, Object> dataMap = objectMapper.convertValue(eventData, Map.class);
                    documentService.deleteDocument(dataMap.get("documentId").toString(), dataMap.get("collectionName").toString(), dataMap.get("databaseName").toString());
                }
                case INDEX_CREATED -> {
                    Map<String, Object> dataMap = objectMapper.convertValue(eventData, Map.class);
                    indexService.saveIndex(new IndexDTO(dataMap.get("databaseName").toString(), dataMap.get("collectionName").toString(), dataMap.get("propertyName").toString()));
                }
                case INDEX_DELETED -> {
                    Map<String, Object> dataMap = objectMapper.convertValue(eventData, Map.class);
                    indexService.deleteIndex(dataMap.get("databaseName").toString(), dataMap.get("collectionName").toString(), dataMap.get("propertyName").toString());
                }
                case USER_CREATED -> userService.save(objectMapper.convertValue(eventData, UserDTO.class));
                case USER_DELETED -> userService.delete(objectMapper.convertValue(eventData, String.class));
            }
            gossipService.addEvent(event);
        } catch (DBOperationException | JsonProcessingException e) {
            LOGGER.error(e.getMessage());
        }
    }


}