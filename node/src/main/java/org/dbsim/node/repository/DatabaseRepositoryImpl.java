package org.dbsim.node.repository;

import org.dbsim.node.exception.DBOperationException;
import org.dbsim.node.enums.DBExceptionErrorType;
import org.dbsim.node.model.db.Database;
import org.dbsim.node.model.db.MetaData;
import org.dbsim.node.util.persistence.FileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DatabaseRepositoryImpl implements DatabaseRepository {
    @Autowired
    private FileSystem fileSystem;


    @Override
    public Database saveDatabase(Database database, MetaData metaData) throws DBOperationException {
        String databasePath = getDatabasePath(database.getName());

        if(!fileSystem.doesDirectoryExist("databases"))
            fileSystem.createDirectory("databases");

        // check if the database already exists
        if(fileSystem.doesDirectoryExist(databasePath))
            throw new DBOperationException("Database already exists", DBExceptionErrorType.RESOURCE_ALREADY_EXISTS);

        // create the directory
        if(!fileSystem.createDirectory(databasePath))
            throw new DBOperationException("Database directory creation failed", DBExceptionErrorType.INTERNAL);


        if(!fileSystem.createFile(databasePath, database.getName() + ".json", metaData.toString()))
            throw new DBOperationException("Database metadata creation failed", DBExceptionErrorType.INTERNAL);

        return database;
    }
    @Override
    public void deleteDatabase(String databaseName) throws DBOperationException {
        String databasePath = getDatabasePath(databaseName);

        // check if the database exists
        if(!fileSystem.doesDirectoryExist(databasePath))
            throw new DBOperationException("Database does not exist", DBExceptionErrorType.NOT_FOUND);

        // delete the directory and its contents
        if(!fileSystem.deleteDirectory(databasePath))
            throw new DBOperationException("Database deletion failed", DBExceptionErrorType.INTERNAL);
    }

    private String getDatabasePath(String databaseName) {
        return "databases/" + databaseName;
    }

}
