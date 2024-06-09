package com.dbsim.demo.model.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class Response {

    @JsonProperty("message")
    private String message;
    @JsonProperty("status")
    private int status;
    @JsonProperty("data")
    private Map<String, Object> data;

    private Response(String message, int status) {
        this.message = message;
        this.status = status;
    }

    public static Builder builder(String message, int status) {
        return new Builder(message, status);
    }

    private Response() {}


    public String getMessage() {
        return message;
    }


    public int getStatus() {
        return status;
    }
    public Map<String, Object> getData() {
        return data;
    }

    public static class Builder {
        private final Response response;

        public Builder(String message, int status) {
            if (message == null || status < 0) {
                throw new IllegalArgumentException("Message and status must not be null");
            }
            response = new Response(message, status);
        }

        public Builder withData(String key, Object value) {
            if (key == null || value == null) {
                throw new IllegalArgumentException("Key and value must not be null");
            }
            if (response.data == null) {
                response.data = new HashMap<>();
            }
            response.data.put(key, value);
            return this;
        }

        public Response build() {
            return response;
        }

    }
}
