package org.dbsim.node.model.db;

import org.dbsim.node.model.db.MetaData;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Collection {
    private String collectionName;
    private String databaseName;
    private Map<String, Object> schema;

    public Collection() {
    }

    public Collection(String collectionName, String databaseName, Map<String, Object> schema) {
        this.collectionName = collectionName;
        this.databaseName = databaseName;
        this.schema = schema;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public Map<String, Object> getSchema() {
        return schema;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public void setSchema(Map<String, Object> schema) {
        this.schema = schema;
    }
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }


}
