package com.dbsim.bootstrapping.service;

import com.dbsim.bootstrapping.dto.JoinDTO;
import com.dbsim.bootstrapping.model.message.ApiResponse;
import com.dbsim.bootstrapping.model.node.BootstrappingNode;
import com.dbsim.bootstrapping.model.node.NeighborNode;
import com.dbsim.bootstrapping.util.communication.HTTPRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class UserService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private BootstrappingNode bootstrappingNode;

    public ApiResponse<Object> joinCluster(JoinDTO request) {
        if (bootstrappingNode.getNeighborNodes().isEmpty()) {
            throw new IllegalStateException("No neighbor nodes available");
        }
        NeighborNode neighborNode = bootstrappingNode.getNextNeighborNode();
        if (neighborNode == null) {
            throw new IllegalStateException("Neighbor node is null" + neighborNode);
        }
        String url = "http://" + neighborNode.getHostname() + ":" + neighborNode.getApiPort() + "/login";

        Map<String, String> data = Map.of("adminUsername", "admin", "adminPassword", "admin",
                "username", request.getUsername(), "password", request.getPassword());

        ResponseEntity<ApiResponse<Object>> responseEntity = null;
        try {
            responseEntity = HTTPRequest.performRequest(restTemplate, url, HttpMethod.POST, data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (responseEntity == null || responseEntity.getBody() == null) {
            throw new IllegalStateException("Response entity or body is null");
        }
        return responseEntity.getBody();

        }
}
