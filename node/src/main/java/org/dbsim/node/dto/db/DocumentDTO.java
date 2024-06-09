package org.dbsim.node.dto.db;

import java.util.Map;

public class DocumentDTO {
    private String databaseName;
    private String collectionName;
    private String documentId;
    private int version;
    private Map<String, Object> document;

    public DocumentDTO() {
    }

    public DocumentDTO(String databaseName, String collectionName, String documentId, Map<String, Object> document, int version) {
        this.databaseName = databaseName;
        this.collectionName = collectionName;
        this.documentId = documentId;
        this.document = document;
        this.version = version;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
    public String getDocumentId() {
        return documentId;
    }

    public Map<String, Object> getDocument() {
        return document;
    }

    public int getVersion() {
        return version;
    }
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setDocument(Map<String, Object> document) {
        this.document = document;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}