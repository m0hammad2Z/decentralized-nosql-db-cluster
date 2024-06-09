package org.dbsim.node.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dbsim.node.enums.DBExceptionErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.dbsim.node.model.db.Document;
import org.dbsim.node.exception.DBOperationException;
import org.dbsim.node.model.db.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dbsim.node.util.persistence.*;
@Repository
public class DocumentRepositoryImpl implements DocumentRepository{
    @Autowired
    private FileSystem fileSystem;
    @Autowired
    private IndexRepository indexRepository;

    public Document saveDocument(Document document)  {
        String documentPath = getDocumentPath(document);

        // check if the document already exists
        if(fileSystem.doesFileExist(documentPath, document.getDocumentId() + ".json"))
            throw new DBOperationException("Document already exists", DBExceptionErrorType.RESOURCE_ALREADY_EXISTS);

        // check if the collection exists
        if(!fileSystem.doesDirectoryExist(documentPath))
            throw new DBOperationException("Collection does not exist", DBExceptionErrorType.NOT_FOUND);

        // create the file
        ObjectMapper mapper = new ObjectMapper();
        try {
            String documentJson = mapper.writeValueAsString(document);
            if(!fileSystem.createFile(documentPath, document.getDocumentId() + ".json", documentJson))
                throw new DBOperationException("Document creation failed", DBExceptionErrorType.INTERNAL);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return document;
    }

    public Document updateDocument(Document document)  {
        String documentPath = getDocumentPath(document);

        // check if the document exists
        if(!fileSystem.doesFileExist(documentPath, document.getDocumentId() + ".json"))
            throw new DBOperationException("Document does not exist", DBExceptionErrorType.NOT_FOUND);

        // Check and update the version
        Document existingDocument = findDocumentById(document.getDocumentId(), document.getCollectionName(), document.getDatabaseName());
        if(existingDocument.getVersion() != document.getVersion())
            throw new DBOperationException("Document version mismatch", DBExceptionErrorType.INVALID_INPUT);

        document.incrementVersion();

        // update the file
        ObjectMapper mapper = new ObjectMapper();
        try {
            String documentJson = mapper.writeValueAsString(document);
            if(!fileSystem.createFile(documentPath, document.getDocumentId() + ".json", documentJson))
                throw new DBOperationException("Document update failed", DBExceptionErrorType.INTERNAL);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return document;
    }

    public void deleteDocument(String documentId, String collectionName, String databaseName) {
        String documentPath = getDocumentPath(databaseName, collectionName);

        // check if the document exists
        if(!fileSystem.doesFileExist(documentPath, documentId + ".json"))
            throw new DBOperationException("Document does not exist", DBExceptionErrorType.NOT_FOUND);

        // delete the file
        if(!fileSystem.deleteFile(documentPath, documentId + ".json"))
            throw new DBOperationException("Document deletion failed", DBExceptionErrorType.INTERNAL);
    }


    public Document findDocumentById(String id, String collectionName, String databaseName) {
        if(id == null || id.isEmpty() || id.isBlank())
            throw new DBOperationException("Wrong document id", DBExceptionErrorType.INVALID_INPUT);

        if(!fileSystem.doesFileExist(getDocumentPath(databaseName, collectionName), id + ".json"))
            throw new DBOperationException("Document does not exist", DBExceptionErrorType.NOT_FOUND);

        String documentContent = fileSystem.getFileContent(getDocumentPath(databaseName, collectionName), id + ".json").orElseThrow(() -> new DBOperationException("Document does not exist", DBExceptionErrorType.NOT_FOUND));

        ObjectMapper mapper = new ObjectMapper();
        Document document = null;
        try {
            document = mapper.readValue(documentContent, Document.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return document;
    }


    public List<Document> getAllDocuments(String collectionName, String databaseName) {
        String documentPath = getDocumentPath(databaseName, collectionName);

        // check if the collection exists
        if(!fileSystem.doesDirectoryExist(documentPath))
            throw new DBOperationException("Collection does not exist", DBExceptionErrorType.NOT_FOUND);

        List<File> files = fileSystem.getFiles(documentPath);
        List<Document> documents = new ArrayList<>();
        for(File file : files){
            if(!file.getName().endsWith(".json") || file.getName().equals(collectionName + ".json"))
                continue;
            String documentContent = fileSystem.getFileContent(documentPath, file.getName()).orElseThrow(() -> new DBOperationException("Document does not exist", DBExceptionErrorType.NOT_FOUND));

            ObjectMapper mapper = new ObjectMapper();
            Document document = null;
            try {
                document = mapper.readValue(documentContent, Document.class);
                documents.add(document);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return documents;
    }


    public List<Document> findDocumentsByProperty(String property, String value, String collectionName, String databaseName) {
        try {
            // Try to find an index for the property
            Index index = indexRepository.findIndexByProperty(property, collectionName, databaseName);
            Set<String> documentIds = index.get(value);

            // If the index does not contain the value, return an empty list
            if (documentIds == null) {
                return new ArrayList<>();
            }

            // Retrieve the documents with the IDs
            List<Document> documents = new ArrayList<>();
            for (String documentId : documentIds) {
                Document document = findDocumentById(documentId, collectionName, databaseName);
                documents.add(document);
            }
            return documents;
        } catch (DBOperationException e) {
            // If the index does not exist, fall back to scanning all documents
            return new ArrayList<>(scanAllDocuments(property, value, collectionName, databaseName));
        }
    }


    private List<Document> scanAllDocuments(String property, String value, String collectionName, String databaseName) {
        String documentPath = getDocumentPath(databaseName, collectionName);

        // check if the collection exists
        if(!fileSystem.doesDirectoryExist(documentPath))
            throw new DBOperationException("Collection does not exist", DBExceptionErrorType.NOT_FOUND);

        List<File> files = fileSystem.getFiles(documentPath);
        List<Document> documents = new ArrayList<>();
        for(File file : files){
            if(!file.getName().endsWith(".json") || file.getName().equals(collectionName + ".json"))
                continue;
            String documentContent = fileSystem.getFileContent(documentPath, file.getName()).orElse(null);
            assert documentContent != null;

            ObjectMapper mapper = new ObjectMapper();
            Document document = null;
            try {
                document = mapper.readValue(documentContent, Document.class);
                if(document.getDocument().containsKey(property) && document.getDocument().get(property).equals(value)){
                    documents.add(document);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return documents;
    }
    // ------------------- Private methods -------------------

    private String getDocumentPath(Document document) {
        return "databases/" + document.getDatabaseName() + "/" + document.getCollectionName();
    }

    private String getDocumentPath(String databaseName, String collectionName) {
        return "databases/" + databaseName + "/" + collectionName;
    }

    private String getIndexPath(Index index) {
        return "databases/" + index.getDatabaseName() + "/" + index.getCollectionName() + "/indexes/" + index.getPropertyName();
    }


}
