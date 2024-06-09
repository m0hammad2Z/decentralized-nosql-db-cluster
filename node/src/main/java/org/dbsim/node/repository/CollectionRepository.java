package org.dbsim.node.repository;

import org.dbsim.node.model.db.Collection;
import org.dbsim.node.model.db.MetaData;

public interface CollectionRepository {
    Collection saveCollection(Collection collection, MetaData metaData);
    void deleteCollection(String databaseName, String collectionName) ;
    Collection getCollectionDetails(String databaseName, String collectionName);
}
