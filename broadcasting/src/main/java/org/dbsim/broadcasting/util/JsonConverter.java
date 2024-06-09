package org.dbsim.broadcasting.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dbsim.broadcasting.api.Message;
import org.dbsim.broadcasting.api.Topic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsonConverter {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String convertToJSON(Message message, Topic topic) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            String topicJson = objectMapper.writeValueAsString(topic);

            Map<String, Object> map = new HashMap<>();
            map.put("message", messageJson);
            map.put("topic", topicJson);

            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            System.out.println("Failed to convert to JSON: " + e.getMessage());
            return null;
        }
    }

    public static <T> T convertFromJSON(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            System.out.println("Failed to convert from JSON: " + e.getMessage());
            return null;
        }
    }
}