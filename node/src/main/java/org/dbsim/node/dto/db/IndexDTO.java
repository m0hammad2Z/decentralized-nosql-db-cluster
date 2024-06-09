package org.dbsim.node.dto.db;

import org.dbsim.node.util.btree.BTree;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Set;

public class IndexDTO {
    private String databaseName;

    private String collectionName;

    private String propertyName;

    private BTree<String, Set<String>> bTree;

    public IndexDTO() {
    }
    public IndexDTO(String databaseName, String collectionName, String propertyName) {
        this.databaseName = databaseName;
        this.collectionName = collectionName;
        this.propertyName = propertyName;
        this.bTree = new BTree<>(3);
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

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}