package com.dbsim.bootstrapping.util.communication;

import com.dbsim.bootstrapping.model.message.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

public class HTTPRequest {
    public static ResponseEntity<ApiResponse<Object>> performRequest(RestTemplate restTemplate, String url, HttpMethod method, Map<String, String> data) throws JsonProcessingException {
        Objects.requireNonNull(url, "URL cannot be null");
        if (url.isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty");
        }

        Objects.requireNonNull(method, "Method cannot be null");

        Objects.requireNonNull(data, "Data cannot be null");

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);
        restTemplate.setRequestFactory(requestFactory);


        HttpEntity<Map<String, String>> rq = new HttpEntity<>(data);
        ResponseEntity<ApiResponse<Object>> responseEntity;

        try {
            responseEntity = restTemplate.exchange(url, method, rq, new ParameterizedTypeReference<ApiResponse<Object>>() {});
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            ObjectMapper mapper = new ObjectMapper();
            ApiResponse<Object> r;
            if (e.getResponseBodyAsString().isEmpty()) {
                r = new ApiResponse<>(false, "Response body is empty", null);
            } else {
                r = mapper.readValue(e.getResponseBodyAsString(), new TypeReference<ApiResponse<Object>>() {});
            }
            responseEntity = ResponseEntity.status(e.getRawStatusCode()).body(r);
        } catch (ResourceAccessException e) {
            ApiResponse<Object> r = new ApiResponse<>(false, "Failed to connect to server", null);
            responseEntity = ResponseEntity.status(500).body(r);
        } catch (Exception e) {
            ApiResponse<Object> r = new ApiResponse<>(false, "Internal server error", null);
            responseEntity = ResponseEntity.status(500).body(r);
        }

        return responseEntity;
    }
}
