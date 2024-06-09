package org.dbsim.node.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dbsim.node.exception.DBOperationException;
import org.dbsim.node.enums.DBExceptionErrorType;
import org.dbsim.node.model.db.Collection;
import org.dbsim.node.model.db.MetaDataBuilder;
import org.dbsim.node.util.persistence.FileSystem;
import org.dbsim.node.model.db.MetaData;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Map;

@Repository
public class CollectionRepositoryImpl implements CollectionRepository {

    @Autowired
    private FileSystem fileSystem;
    public Collection saveCollection(Collection collection, MetaData metaData)  {
        String collectionPath = getCollectionPath(collection);

        // check if the database exists
        if(!fileSystem.doesDirectoryExist("databases/" + collection.getDatabaseName() + "/"))
            throw new DBOperationException("Database does not exist", DBExceptionErrorType.NOT_FOUND);

        // check if the collection already exists
        if(fileSystem.doesDirectoryExist(collectionPath))
            throw new DBOperationException("Collection already exists", DBExceptionErrorType.RESOURCE_ALREADY_EXISTS);

        // create the directory
        if(!fileSystem.createDirectory(collectionPath))
            throw new DBOperationException("Collection directory creation failed", DBExceptionErrorType.INTERNAL);

        // create the metadata file
        if(!fileSystem.createFile(collectionPath, collection.getCollectionName() + ".json", metaData.toString()))
            throw new DBOperationException("Collection metadata creation failed", DBExceptionErrorType.INTERNAL);

        return collection;
    }

    public void deleteCollection(String collectionName, String databaseName)  {
        String collectionPath = getCollectionPath(databaseName, collectionName);

        // check if the database exists
        if(!fileSystem.doesDirectoryExist("databases/" + databaseName + "/"))
            throw new DBOperationException("Database does not exist", DBExceptionErrorType.NOT_FOUND);

        // check if the collection exists
        if(!fileSystem.doesDirectoryExist(collectionPath))
            throw new DBOperationException("Collection does not exist", DBExceptionErrorType.NOT_FOUND);

        // delete the directory and its contents
        if(!fileSystem.deleteDirectory(collectionPath))
            throw new DBOperationException("Collection deletion failed", DBExceptionErrorType.INTERNAL);
    }

    public Collection getCollectionDetails(String databaseName, String collectionName)  {
        String collectionPath = getCollectionPath(databaseName, collectionName);

        // check if the collection exists and get the metadata
        if(!fileSystem.doesDirectoryExist(collectionPath))
            throw new DBOperationException("Database or collection does not exist", DBExceptionErrorType.NOT_FOUND);

        // Get the metadata
        String metadata = fileSystem.getFileContent(collectionPath, collectionName + ".json").
                orElseThrow(() -> new DBOperationException("Collection metadata not found", DBExceptionErrorType.INTERNAL));
        MetaData metaData = MetaDataBuilder.createMetaData(collectionName, new JSONObject(metadata)).build();

        // Get the schema from the metadata
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> schemaMap;
        try {
            schemaMap = mapper.readValue(metaData.getMetadata().get("schema").toString(), Map.class);
        } catch (IOException e) {
            throw new DBOperationException("Error parsing schema", DBExceptionErrorType.INTERNAL);
        }

        return new Collection(collectionName, databaseName, schemaMap);
    }

    // ----------------- Private methods -----------------
    private String getCollectionPath(Collection collection) {
        return "databases/" + collection.getDatabaseName() + "/" + collection.getCollectionName();
    }

    private String getCollectionPath(String databaseName, String collectionName) {
        return "databases/" + databaseName + "/" + collectionName;
    }



}
