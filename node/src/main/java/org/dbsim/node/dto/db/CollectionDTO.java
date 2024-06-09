package org.dbsim.node.dto.db;

import org.dbsim.node.model.db.MetaData;
import org.json.JSONObject;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

public class CollectionDTO {

    private String collectionName;
    private String databaseName;
    private Map<String, Object> schema;


    public CollectionDTO() {
    }

    public CollectionDTO(String collectionName, String databaseName, Map<String, Object> schema) {
        this.collectionName = collectionName;
        this.databaseName = databaseName;
        this.schema = schema;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public Map<String, Object> getSchema() {
        return schema;
    }

    public void setSchema(Map<String, Object> schema) {
        this.schema = schema;
    }

}
