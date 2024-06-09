package org.dbsim.node.repository;

import org.dbsim.node.exception.DBOperationException;
import org.dbsim.node.model.db.Database;
import org.dbsim.node.model.db.MetaData;

public interface DatabaseRepository {

    Database saveDatabase(Database database, MetaData metaData) throws DBOperationException;

    void deleteDatabase(String databaseName) ;
}
