package org.dbsim.node.model.db;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.dbsim.node.model.node.MainNode;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Document {
    private String documentId;
    private Map<String, Object> document;
    private String collectionName;
    private String databaseName;
    private int version;
    private int affinityNode;

    public Document(){
    }

    public Document(String documentId, Map<String, Object> document, String collectionName, String databaseName) {
        this.documentId = documentId;
        this.document = document;
        this.collectionName = collectionName;
        this.databaseName = databaseName;
        this.version = Integer.parseInt(document.getOrDefault("version", "0").toString());
        this.affinityNode = Integer.parseInt(document.getOrDefault("affinityNode", "-1").toString());
    }

    public String getDocumentId() {
        return documentId;
    }

    public Map<String, Object> getDocument() {
        return document;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setDocument(Map<String, Object> document) {
        this.document = document;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void incrementVersion() {
        this.version++;
    }

    public int getAffinityNode() {
        return affinityNode;
    }

    public void setAffinityNode(int affinityNode) {
        this.affinityNode = affinityNode;
    }
}