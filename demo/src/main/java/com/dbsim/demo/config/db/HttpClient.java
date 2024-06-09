package com.dbsim.demo.config.db;

import com.dbsim.demo.model.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;


import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import com.dbsim.demo.util.Constants;

@Component
public class HttpClient {

    private ObjectMapper objectMapper = new ObjectMapper();
    private String token;
    private String url;
    private boolean connected = false;

    @Autowired
    private RestTemplate restTemplate;

    public HttpClient() {
        this.token = "";
        this.url = "";
    }


    @PostConstruct
    private void init() {
        String username = "admin";
        String password = "admin";
        Map<String,Object> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        ApiResponse<Map<String, String>> response = null;
        int attempts = 0;
        while (attempts < 5) {
            try {
                response = (ApiResponse<Map<String, String>>) this.sendRequest("http://" + Constants.BOOTSTRAPPING_NODE_SERVER + ":" + Constants.BOOTSTRAPPING_NODE_PORT + "/login", null, body, "POST").getBody();
                if (response == null || response.getData() == null) {
                    throw new RuntimeException("Received null or empty response");
                }
                Map<String, String> data = response.getData();

                token =  data.get("token");
                url =  data.get("nodeUrl");

                System.out.println("Token: " + token);
                System.out.println("URL: " + url);
                if (token.isEmpty() || url.isEmpty()) {
                    throw new RuntimeException("Token or URL is empty");
                }
                // If we reach this point, it means the initialization was successful, so we break the loop
                connected = true;
                break;
            } catch (Exception e){
                attempts++;
                if (attempts >= 5) {
                    throw new RuntimeException("Error initializing HttpClient: " + e.getMessage());
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread was interrupted: " + ie.getMessage());
                }
            }
        }
    }


    public ResponseEntity<ApiResponse<?>> sendRequest(String url, Map<String, String> headers, Map<String, Object> body, String requestMethod) {
        url = this.url + url;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Set headers
        if (headers != null) {
            for (String key : headers.keySet()) {
                httpHeaders.set(key, headers.get(key).toString());
            }
        }

        // Set token
        if (token != null) {
            httpHeaders.set("Authorization", "Bearer " + token);
        }

        // Set body
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, httpHeaders);

        ResponseEntity<ApiResponse<?>> responseEntity = switch (requestMethod) {
            case "GET" -> restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<ApiResponse<?>>() {});
            case "POST" -> restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<ApiResponse<?>>() {});
            case "PUT" -> restTemplate.exchange(url, HttpMethod.PUT, entity, new ParameterizedTypeReference<ApiResponse<?>>() {});
            case "DELETE" -> restTemplate.exchange(url, HttpMethod.DELETE, entity, new ParameterizedTypeReference<ApiResponse<?>>() {});
            default -> throw new IllegalArgumentException("Invalid request method: " + requestMethod);
        };

        return responseEntity;
    }

    public boolean isConnected() {
        return connected;
    }
}