package org.dbsim.node.repository;

import org.dbsim.node.exception.DBOperationException;
import org.dbsim.node.enums.DBExceptionErrorType;
import org.dbsim.node.model.db.Index;
import org.dbsim.node.util.persistence.FileSystem;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class IndexRepositoryImpl implements IndexRepository {
    @Autowired
    private FileSystem fileSystem;

    @Override
    public Index saveIndex(Index index) {
        // Check if database exists
        if (!fileSystem.doesDirectoryExist("databases/" + index.getDatabaseName())) {
            throw new DBOperationException("Database does not exist", DBExceptionErrorType.NOT_FOUND);
        }

        // Check if collection exists
        if (!fileSystem.doesDirectoryExist("databases/" + index.getDatabaseName() + "/" + index.getCollectionName())) {
            throw new DBOperationException("Collection does not exist", DBExceptionErrorType.NOT_FOUND);
        }

        // Check if index folder exists
        if (!fileSystem.doesDirectoryExist(getIndexFilePath(index))) {
            fileSystem.createDirectory(getIndexFilePath(index));
        }

        // If index file exists, update it. If not, create a new one.
        if (fileSystem.doesFileExist(getIndexFilePath(index), index.getPropertyName() + ".idx") && index.getVersion() > 0) {

            if (!fileSystem.createBinaryFile(getIndexFilePath(index), index.getPropertyName() + ".idx", index)) {
                throw new DBOperationException("Index update failed", DBExceptionErrorType.INTERNAL);
            }

        } else {
            addExistingDocumentsToIndex(index);


            if (!fileSystem.createBinaryFile(getIndexFilePath(index), index.getPropertyName() + ".idx", index)) {
                throw new DBOperationException("Index creation failed", DBExceptionErrorType.INTERNAL);
            }
        }

        return index;
    }

    // Add the already documented that has the property to the index before saving it
    private void addExistingDocumentsToIndex(Index index) {
        String documentPath = "databases/" + index.getDatabaseName() + "/" + index.getCollectionName();

        List<File> files = fileSystem.getFiles(documentPath);
        for(File file : files){
            if(!file.getName().endsWith(".json") || file.getName().equals(index.getCollectionName() + ".json"))
                continue;
            String documentContent = fileSystem.getFileContent(documentPath, file.getName()).orElse(null);
            assert documentContent != null;
            JSONObject jsonObject = new JSONObject(documentContent).getJSONObject("document");
            if(jsonObject.has(index.getPropertyName())){
                index.put(jsonObject.get(index.getPropertyName()).toString(), file.getName().replace(".json", ""), index.getVersion());
            }
        }
    }

    @Override
    public void deleteIndex(String databaseName, String collectionName, String propertyName) {
        Index index = new Index(databaseName, collectionName, propertyName);
        if (!fileSystem.doesFileExist(getIndexFilePath(index), index.getPropertyName() + ".idx")) {
            throw new DBOperationException("Index does not exist", DBExceptionErrorType.NOT_FOUND);
        }

        if (!fileSystem.deleteFile(getIndexFilePath(index), index.getPropertyName() + ".idx")) {
            throw new DBOperationException("Index deletion failed", DBExceptionErrorType.INTERNAL);
        }
    }

    @Override
    public Index findIndexByProperty(String propertyName, String collectionName, String databaseName) throws DBOperationException {
        Optional<Serializable> content = fileSystem.getBinaryFileContent(getIndexFilePath(new Index(databaseName, collectionName, propertyName)), propertyName + ".idx");
        if (content.isEmpty()) {
            throw new DBOperationException("Index does not exist", DBExceptionErrorType.NOT_FOUND);
        }

        return (Index) content.get();
    }

    @Override
    public List<Index> findIndexByCollection(String collectionName, String databaseName) {
        List<Index> indexes = new ArrayList<>();
        List<File> indexFiles = fileSystem.getFiles(getIndexFilePath(new Index(databaseName, collectionName, "")));
        for (File indexFile : indexFiles) {
            String fileName = indexFile.getName();
            Optional<Serializable> content = fileSystem.getBinaryFileContent(getIndexFilePath(new Index(databaseName, collectionName, "")), fileName);
            content.ifPresent(serializable -> indexes.add((Index) serializable));
        }

        return indexes;
    }

    // ------------------- Private methods ------------------- //
    private String getIndexFilePath(Index index) {
        return "databases/" + index.getDatabaseName() + "/" + index.getCollectionName() + "/indexes/";
    }
}
