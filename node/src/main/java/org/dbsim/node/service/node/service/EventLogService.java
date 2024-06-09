package org.dbsim.node.service.node.service;

import org.dbsim.node.model.event.DBEvent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventLogService {
    private final List<DBEvent> eventLog = new ArrayList<>();


    public void addEvent(DBEvent event) {
        eventLog.add(event);
    }

    public List<DBEvent> getEvents() {
        return eventLog;
    }

    public List<DBEvent> getEventsSince(long timestamp) {
        List<DBEvent> events = new ArrayList<>();
        for (DBEvent event : new ArrayList<>(eventLog)) {
            if (event.getTimestamp() >= timestamp) {
                events.add(event);
            }
        }
        return events;
    }
}
