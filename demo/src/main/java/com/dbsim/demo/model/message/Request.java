package com.dbsim.demo.model.message;

import java.util.HashMap;
import java.util.Map;

public class Request {
    private Map<String, Object> data;
    private Request() {
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Object get(String key) {
        if(key == null) {
            return null;
        }
        return data.get(key) == null ? null : data.get(key);
    }

    public static class Builder {
        private final Request request;

        public Builder() {
            request = new Request();
            request.data = new HashMap<>();
        }

        public Builder withData(String key, Object value) {
            request.data.put(key, value);
            return this;
        }

        public Request build() {
            return request;
        }
    }
}