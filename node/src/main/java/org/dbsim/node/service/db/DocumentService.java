package org.dbsim.node.service.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.dbsim.node.dto.db.CollectionDTO;
import org.dbsim.node.dto.db.DocumentDTO;
import org.dbsim.node.enums.DBEventType;
import org.dbsim.node.model.db.Document;
import org.dbsim.node.model.db.Index;
import org.dbsim.node.model.node.MainNode;
import org.dbsim.node.repository.DocumentRepository;
import org.dbsim.node.service.node.service.DBGossipService;
import org.dbsim.node.util.validation.Validator;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DocumentService {

    private DocumentRepository documentRepository;
    private CollectionService collectionService;
    private IndexService indexService;
    private DBGossipService dbGossipService;
    private MainNode mainNode;
    private RestTemplate restTemplate;
    private ModelMapper modelMapper;

    @Autowired
    public DocumentService(DocumentRepository documentRepository, CollectionService collectionService, IndexService indexService, DBGossipService dbGossipService, MainNode mainNode, RestTemplate restTemplate, ModelMapper modelMapper) {
        this.documentRepository = documentRepository;
        this.collectionService = collectionService;
        this.indexService = indexService;
        this.dbGossipService = dbGossipService;
        this.mainNode = mainNode;
        this.restTemplate = restTemplate;
        this.modelMapper = modelMapper;
    }


    public DocumentDTO saveDocument(DocumentDTO documentdto) {
        Validator.isNotNull(documentdto, "Document must not be null", true);
        // if document id is not provided, generate a new one
        if (documentdto.getDocumentId() == null || documentdto.getDocumentId().isEmpty()) {
            documentdto.setDocumentId(UUID.randomUUID().toString());
        }
        return saveDocumentHasId(documentdto);
    }

    public DocumentDTO saveDocumentHasId(DocumentDTO documentdto){
        Document document = modelMapper.map(documentdto, Document.class);
        Validator.isNotNull(document, "Document must not be null", true);
        Validator.isValidString(document.getDocumentId(), "Document id must not be null or empty", true);
        Validator.isValidString(document.getDatabaseName(), "Database name must not be null or empty", true);
        Validator.isValidString(document.getCollectionName(), "Collection name must not be null or empty", true);
        Validator.isNotNull(document.getDocument(), "Document must not be null", true);

        CollectionDTO col = collectionService.getCollectionDetails(document.getCollectionName(), document.getDatabaseName());

        boolean isValidSchema = Validator.isValidSchema(new JSONObject(col.getSchema()), false);
        if(isValidSchema) {
            Validator.isValidDocument(new JSONObject(col.getSchema()), new JSONObject(document.getDocument()), true);
        }

        int affinityNode = mainNode.getAffinityNodeId(document.getDocumentId());
        document.setAffinityNode(affinityNode);
        documentRepository.saveDocument(document);

        // Update indexes
        List<Index> indexes = indexService.getIndexesByCollection(document.getDatabaseName(), document.getCollectionName());
        for (Index index : indexes) {
            if (document.getDocument().containsKey(index.getPropertyName())) {
                indexService.put(document.getDatabaseName(), document.getCollectionName(), index.getPropertyName(), document.getDocument().get(index.getPropertyName()).toString(), document.getDocumentId());
            }
        }

        dbGossipService.addEvent(DBEventType.DOCUMENT_CREATED, documentdto);

        return documentdto;
    }




    public DocumentDTO updateDocument(DocumentDTO documentdto) {
        Document document = modelMapper.map(documentdto, Document.class);
        Validator.isNotNull(document, "Document must not be null", true);
        Validator.isValidString(document.getDocumentId(), "Document id must not be null or empty", true);
        Validator.isValidString(document.getDatabaseName(), "Database name must not be null or empty", true);
        Validator.isValidString(document.getCollectionName(), "Collection name must not be null or empty", true);
        Validator.isNotNull(document.getDocument(), "Document must not be null", true);

        CollectionDTO col = collectionService.getCollectionDetails(document.getCollectionName(), document.getDatabaseName());

        boolean isValidSchema = Validator.isValidSchema(new JSONObject(col.getSchema()), false);
        if(isValidSchema) {
            Validator.isValidDocument(new JSONObject(col.getSchema()), new JSONObject(document.getDocument()), true);
        }


        // Update the document in the database
        Document oldDoc = null;
        try {
            oldDoc = documentRepository.findDocumentById(document.getDocumentId(), document.getCollectionName(), document.getDatabaseName());
            documentRepository.updateDocument(document);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // Update indexes with new document
        List<Index> indexes = indexService.getIndexesByCollection(document.getDatabaseName(), document.getCollectionName());
        for (Index index : indexes) {
            // Remove the old document from the index
            if (oldDoc.getDocument().containsKey(index.getPropertyName())) {
                indexService.remove(document.getDatabaseName(), document.getCollectionName(), index.getPropertyName(), oldDoc.getDocument().get(index.getPropertyName()).toString(), document.getDocumentId());
            }
            if (document.getDocument().containsKey(index.getPropertyName())) {
                indexService.put(document.getDatabaseName(), document.getCollectionName(), index.getPropertyName(), document.getDocument().get(index.getPropertyName()).toString(), document.getDocumentId());
            }
        }

        dbGossipService.addEvent(DBEventType.DOCUMENT_UPDATED, documentdto);

        return documentdto;
    }

    public void deleteDocument(String documentId, String collectionName, String databaseName) throws JsonProcessingException {
        Validator.isValidString(databaseName, "Database name must not be null or empty", true);
        Validator.isValidString(collectionName, "Collection name must not be null or empty", true);
        Validator.isValidString(documentId, "Document id must not be null or empty", true);

        // Remove indexes for the document
        List<Index> indexes = indexService.getIndexesByCollection(databaseName, collectionName);
        for (Index index : indexes) {
            if (documentRepository.findDocumentById(documentId, collectionName, databaseName).getDocument().containsKey(index.getPropertyName())) {
                indexService.remove(databaseName, collectionName, index.getPropertyName(), documentRepository.findDocumentById(documentId, collectionName, databaseName).getDocument().get(index.getPropertyName()).toString(), documentId);
            }
        }

        documentRepository.deleteDocument(documentId, collectionName, databaseName);

        dbGossipService.addEvent(DBEventType.DOCUMENT_DELETED, Map.of("documentId", documentId, "collectionName", collectionName, "databaseName", databaseName));
    }

    public DocumentDTO getDocumentById(String documentId, String collectionName, String databaseName) throws JsonProcessingException {
        Validator.isValidString(databaseName, "Database name must not be null or empty", true);
        Validator.isValidString(collectionName, "Collection name must not be null or empty", true);
        Validator.isValidString(documentId, "Document id must not be null or empty", true);

        return modelMapper.map(documentRepository.findDocumentById(documentId, collectionName, databaseName), DocumentDTO.class);
    }

    public List<DocumentDTO> getDocumentsByProperty(String property, String value, String collectionName, String databaseName) {
        Validator.isValidString(databaseName, "Database name must not be null or empty", true);
        Validator.isValidString(collectionName, "Collection name must not be null or empty", true);
        Validator.isValidString(property, "Property must not be null or empty", true);
        Validator.isValidString(value, "Value must not be null or empty", true);

        return documentRepository.findDocumentsByProperty(property, value, collectionName, databaseName).stream().map(document -> modelMapper.map(document, DocumentDTO.class)).toList();
    }

    public List<DocumentDTO> getAlldocuments(String collectionName, String databaseName) {
        Validator.isValidString(databaseName, "Database name must not be null or empty", true);
        Validator.isValidString(collectionName, "Collection name must not be null or empty", true);

        return documentRepository.getAllDocuments(collectionName, databaseName).stream().map(document -> modelMapper.map(document, DocumentDTO.class)).toList();
    }





}