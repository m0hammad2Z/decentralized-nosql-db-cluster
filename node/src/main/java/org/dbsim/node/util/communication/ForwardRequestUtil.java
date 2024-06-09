package org.dbsim.node.util.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.dbsim.node.model.message.ApiResponse;
import org.dbsim.node.model.node.MainNode;
import org.dbsim.node.model.node.NeighborNode;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class ForwardRequestUtil {
    private RestTemplate restTemplate;

    public ForwardRequestUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    // Forward to affinity node
    public  <T> ResponseEntity<ApiResponse<T>> forward(T data, NeighborNode neighborNode, String finalUrl, HttpMethod method)  {
        if (neighborNode == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Neighbor node is null", null));
        }
        String token = getToken(neighborNode);

        String url = "http://" + neighborNode.getHostname() + ":" + neighborNode.getApiPort() + finalUrl;
        if (data == null) {
            data = (T) Map.of();
        }

        ResponseEntity<ApiResponse<Object>> responseEntity;
        try {
            responseEntity = HTTPRequest.performRequest(restTemplate, url, method, data, Map.of("Authorization", "Bearer " + token, "X-Forwarded-Request", "true"));
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error while forwarding request", null));
        }

        if (responseEntity == null || responseEntity.getBody() == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Response entity or body is null", null));
        }

        return ResponseEntity.status(responseEntity.getStatusCode()).body(new ApiResponse<>(responseEntity.getBody().getSuccess(), responseEntity.getBody().getMessage(), (T) responseEntity.getBody().getData()));
    }
    private String getToken(NeighborNode neighborNode) {
        String url = "http://" + neighborNode.getHostname() + ":" + neighborNode.getApiPort() + "/login";
        Map<String, String> data = Map.of("adminUsername", "admin", "adminPassword", "admin",
                "username", "admin", "password", "admin");

        ResponseEntity<ApiResponse<Object>> responseEntity;
        try {
            responseEntity = HTTPRequest.performRequest(restTemplate, url, HttpMethod.POST, data, null);
        } catch (JsonProcessingException e) {
            System.out.println("Error while getting token");
            return new ApiResponse<>(false, "Error while getting token", null).toString();
        }

        if (responseEntity == null || responseEntity.getBody() == null) {
            return new ApiResponse<>(false, "Response entity or body is null", null).toString();
        }

        Map<String, String> response = (Map<String, String>) responseEntity.getBody().getData();
        return response.get("token");
    }
}
