package org.dbsim.node.util.communication;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dbsim.node.model.message.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

public class HTTPRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPRequest.class);

    public static ResponseEntity<ApiResponse<Object>> performRequest(RestTemplate restTemplate, String url, HttpMethod method, Object data, Map<String, String> headers) throws JsonProcessingException {
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

        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }

        HttpEntity<Object> rq = new HttpEntity<>(data, httpHeaders);
        ResponseEntity<ApiResponse<Object>> responseEntity;

        try {
            responseEntity = restTemplate.exchange(url, method, rq, new ParameterizedTypeReference<ApiResponse<Object>>() {});
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            LOGGER.error("HTTP error occurred");
            return ResponseEntity.status(e.getRawStatusCode()).body(new ObjectMapper().readValue(e.getResponseBodyAsString(), new TypeReference<ApiResponse<Object>>() {}));
        } catch (ResourceAccessException e) {
            LOGGER.error("Failed to connect to server");
            return ResponseEntity.status(500).body(new ApiResponse<>(false, "Failed to connect to server", null));
        }

        return responseEntity;
    }
}
