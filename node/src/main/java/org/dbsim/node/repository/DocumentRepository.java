package org.dbsim.node.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.dbsim.node.exception.DBOperationException;
import org.dbsim.node.model.db.Document;

import java.util.List;

public interface DocumentRepository {
    Document saveDocument(Document document) ;
    Document updateDocument(Document document) throws JsonProcessingException;
    void deleteDocument(String documentId, String collectionName, String databaseName) ;
    Document findDocumentById(String id, String collectionName, String databaseName) throws JsonProcessingException;
    List<Document> getAllDocuments(String collectionName, String databaseName) ;
    List<Document> findDocumentsByProperty(String property, String value, String collectionName, String databaseName) ;

}
