package com.dbsim.demo.config.db;

import com.dbsim.demo.model.ApiResponse;
import com.dbsim.demo.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseConnection {
    @Autowired
    private HttpClient httpClient;
    private String database;

    @PostConstruct
    private void init() {
        this.database = "emailAppDemo";
        Map<String, List<String>> collections = Map.of(
                "users", List.of("username", "email"),
                "emails", List.of("sender", "receiver")
        );

        do{
            try {
                createDatabase(database);
                for (Map.Entry<String, List<String>> entry : collections.entrySet()) {
                    String collectionName = entry.getKey();
                    ApiResponse<Map<String, String>> collection = createCollection(database, collectionName);
                    System.out.println(collection);

                    for (String indexName : entry.getValue()) {
                        ApiResponse<Map<String, String>> index = addIndex(database, collectionName, indexName);
                        System.out.println(index);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error initializing database: " + e.getMessage());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        while (!httpClient.isConnected());


    }

    public ApiResponse<Map<String, String>> createDatabase(String databaseName) {
        ResponseEntity<ApiResponse<?>> responseEntity;
        Map<String, Object> request = Map.of("name", databaseName);
        try {
            responseEntity = httpClient.sendRequest("/database/create", null, request, "POST");
            if (responseEntity.getBody().getData() instanceof Map) {
                return (ApiResponse<Map<String, String>>) responseEntity.getBody();
            } else {
                throw new RuntimeException("Unexpected data type");
            }
        } catch (HttpClientErrorException e) {
            try {
                return new ObjectMapper().readValue(e.getResponseBodyAsString(), new TypeReference<ApiResponse<Map<String, String>>>() {});
            } catch (IOException ex) {
                throw new RuntimeException("Error creating database: " + e.getMessage(), e);
            }
        }
    }

    public ApiResponse<Map<String, String>> createCollection(String databaseName, String collectionName) {
        ResponseEntity<ApiResponse<?>> responseEntity;
        Map<String, Object> request = Map.of("databaseName", databaseName, "collectionName", collectionName);
        try {
            responseEntity = httpClient.sendRequest("/collection/create", null, request, "POST");
            if (responseEntity.getBody().getData() instanceof Map) {
                return (ApiResponse<Map<String, String>>) responseEntity.getBody();
            } else {
                throw new RuntimeException("Unexpected data type");
            }
        } catch (HttpClientErrorException e) {
            try {
                return new ObjectMapper().readValue(e.getResponseBodyAsString(), new TypeReference<ApiResponse<Map<String, String>>>() {});
            } catch (IOException ex) {
                throw new RuntimeException("Error creating collection: " + e.getMessage(), e);
            }
        }
    }

    public ApiResponse<Map<String, String>> addIndex(String databaseName, String collectionName, String property) {
        try {
            Map<String, Object> request = Map.of("databaseName", databaseName, "collectionName", collectionName, "propertyName", property);

            ResponseEntity<ApiResponse<?>> responseEntity = httpClient.sendRequest("/index/create", null, request, "POST");
            if (responseEntity.getBody().getData() instanceof Map) {
                return (ApiResponse<Map<String, String>>) responseEntity.getBody();
            } else {
                throw new RuntimeException("Unexpected data type");
            }
        } catch (HttpClientErrorException e) {
            try {
                return new ObjectMapper().readValue(e.getResponseBodyAsString(), new TypeReference<ApiResponse<Map<String, String>>>() {});
            } catch (IOException ex) {
                throw new RuntimeException("Error adding index: " + e.getMessage(), e);
            }
        }
    }
    public ApiResponse<Map<String, Object>> createDocument(String collection, Map<String, Object> document) {
        Map<String, Object> body = new HashMap<>();
        body.put("databaseName", database);
        body.put("collectionName", collection);
        body.put("document", document);
        try {
            return (ApiResponse<Map<String, Object>>) httpClient.sendRequest("/document/create", null, body, "POST").getBody();
        } catch (RestClientException e) {
            throw new RuntimeException("Error creating document: " + e.getMessage(), e);
        }
    }

    public ApiResponse<Map<String, Object>> updateDocument(String collection, String documentId, Map<String, Object> document, int version) {
        Map<String, Object> body = new HashMap<>();
        body.put("databaseName", database);
        body.put("collectionName", collection);
        body.put("documentId", documentId);
        body.put("document", document);
        body.put("version", version);
        try {
            return (ApiResponse<Map<String, Object>>) httpClient.sendRequest("/document/update", null, body, "PUT").getBody();
        } catch (RestClientException e) {
            throw new RuntimeException("Error updating document: " + e.getMessage(), e);
        }
    }

    public ApiResponse<List<Map<String, Object>>> getDocumentsByProperty(String collection, String property, String value) {
        try {
            ResponseEntity<ApiResponse<?>> responseEntity = httpClient.sendRequest("/document/readByProperty/" + database + "/" + collection + "/" + property + "/" + value, null, null, "GET");
            if (responseEntity.getBody().getData() instanceof List) {
                return (ApiResponse<List<Map<String, Object>>>) responseEntity.getBody();
            } else {
                throw new RuntimeException("Unexpected data type");
            }
        } catch (RestClientException e) {
            throw new RuntimeException("Error getting documents by property: " + e.getMessage(), e);
        }
    }

    public ApiResponse<List<Map<String, Object>>> getDocumentById(String collection, String documentId){
        try {
            ResponseEntity<ApiResponse<?>> responseEntity = httpClient.sendRequest("/document/read/" + database + "/" + collection + "/" + documentId, null, null, "GET");
            if (responseEntity.getBody().getData() instanceof List) {
                return (ApiResponse<List<Map<String, Object>>>) responseEntity.getBody();
            } else {
                throw new RuntimeException("Unexpected data type");
            }
        } catch (RestClientException e) {
            throw new RuntimeException("Error getting document by ID: " + e.getMessage(), e);
        }
    }

    public ApiResponse<List<Map<String, Object>>> getAlldocuments(String collection)  {
        try {
            ResponseEntity<ApiResponse<?>> responseEntity = httpClient.sendRequest("/document/readAll/" + database + "/" + collection, null, null, "GET");
            if (responseEntity.getBody().getData() instanceof List) {
                return (ApiResponse<List<Map<String, Object>>>) responseEntity.getBody();
            } else {
                throw new RuntimeException("Unexpected data type");
            }
        } catch (RestClientException e) {
            throw new RuntimeException("Error getting all documents: " + e.getMessage(), e);
        }
    }

    public ApiResponse<Map<String, Object>> deleteDocument(String collection, String documentId)  {
        try {
            ResponseEntity<ApiResponse<?>> responseEntity = httpClient.sendRequest("/document/delete/" + database + "/" + collection + "/" + documentId, null, null, "DELETE");
            if (responseEntity.getBody().getData() instanceof Map) {
                return (ApiResponse<Map<String, Object>>) responseEntity.getBody();
            } else {
                throw new RuntimeException("Unexpected data type");
            }
        } catch (RestClientException e) {
            throw new RuntimeException("Error deleting document: " + e.getMessage(), e);
        }
    }

    public String getDocumentId(String collection, String property, String value) {
        ApiResponse<List<Map<String, Object>>> response = getDocumentsByProperty(collection, property, value);
        List<Map<String, Object>> documents = response.getData();
        if (documents == null || documents.isEmpty()) {
            throw new RuntimeException("Document not found");
        }

        // Get the first document in the response
        Map<String, Object> firstDocument = documents.get(0);

        // The document ID is the key in the documents map
        return (String) firstDocument.get("documentId");
    }

    public int getDocumentVersion(String collection, String property, String value)  {
        ApiResponse<List<Map<String, Object>>> response = getDocumentsByProperty(collection, property, value);
        List<Map<String, Object>> documents = response.getData();
        if (documents == null || documents.isEmpty()) {
            throw new RuntimeException("Document not found");
        }

        // Get the first document in the response
        Map<String, Object> firstDocument = documents.get(0);

        // Extract and return the document version
        return (int) firstDocument.get("version");
    }
}