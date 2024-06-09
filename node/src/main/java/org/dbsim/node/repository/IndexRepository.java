package org.dbsim.node.repository;

import org.dbsim.node.exception.DBOperationException;
import org.dbsim.node.model.db.Index;

import java.util.List;

public interface IndexRepository {
    Index saveIndex(Index index);
    void deleteIndex(String databaseName, String collectionName, String propertyName);

    Index findIndexByProperty(String propertyName, String collectionName, String databaseName);

    List<Index> findIndexByCollection(String collectionName, String databaseName);
}
