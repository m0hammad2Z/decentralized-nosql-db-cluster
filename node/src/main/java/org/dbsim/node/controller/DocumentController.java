package org.dbsim.node.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.dbsim.node.dto.db.DocumentDTO;
import org.dbsim.node.model.message.ApiResponse;
import org.dbsim.node.model.node.MainNode;
import org.dbsim.node.model.node.NeighborNode;
import org.dbsim.node.service.db.DocumentService;
import org.dbsim.node.util.communication.ForwardRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/document")
public class DocumentController {

    private DocumentService documentService;
    private ForwardRequestUtil forwardRequestUtil;
    private MainNode mainNode;

    @Autowired
    public DocumentController(DocumentService documentService, ForwardRequestUtil forwardRequestUtil, MainNode mainNode) {
        this.documentService = documentService;
        this.forwardRequestUtil = forwardRequestUtil;
        this.mainNode = mainNode;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<DocumentDTO>> createDocument(@RequestBody DocumentDTO documentDTO) {
        int affinityNode = mainNode.getAffinityNodeId(documentDTO.getDatabaseName() + documentDTO.getCollectionName());
        if (affinityNode == -1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Affinity node is -1", null));
        }

        if (affinityNode == mainNode.getId()) {
            // Save the document
            DocumentDTO savedDocumentDTO = documentService.saveDocument(documentDTO);
            // Return the saved document
            return ResponseEntity.status(201).body(new ApiResponse<>(true, "Document created successfully", savedDocumentDTO));
        }

        NeighborNode neighborNode = mainNode.getNeighborNode(affinityNode);
        return forwardRequestUtil.forward(documentDTO, neighborNode, "/document/create", HttpMethod.POST);
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<DocumentDTO>> updateDocument(@RequestBody DocumentDTO documentDTO) throws JsonProcessingException {
        int affinityNode = mainNode.getAffinityNodeId(documentDTO.getDatabaseName() + documentDTO.getCollectionName());
        if (affinityNode == -1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Affinity node is -1", null));
        }

        if (affinityNode == mainNode.getId()) {
            // Save the document
            DocumentDTO savedDocumentDTO = documentService.updateDocument(documentDTO);
            // Return the saved document
            return ResponseEntity.status(200).body(new ApiResponse<>(true, "Document updated successfully", savedDocumentDTO));
        }

        NeighborNode neighborNode = mainNode.getNeighborNode(affinityNode);
        return forwardRequestUtil.forward(documentDTO, neighborNode, "/document/update", HttpMethod.PUT);
    }

    @DeleteMapping("/delete/{databaseName}/{collectionName}/{documentId}")
    public ResponseEntity<ApiResponse<DocumentDTO>> deleteDocument(@PathVariable String databaseName, @PathVariable String collectionName, @PathVariable String documentId) throws JsonProcessingException {
        int affinityNode = mainNode.getAffinityNodeId(databaseName + collectionName + documentId);
        if (affinityNode == -1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Affinity node is -1", null));
        }

        if (affinityNode == mainNode.getId()) {
            // Delete the document
            documentService.deleteDocument(documentId, collectionName, databaseName);
            // Return the response
            return ResponseEntity.status(200).body(new ApiResponse<>(true, "Document deleted successfully", null));
        }

        return forwardRequestUtil.forward(null, mainNode.getNeighborNode(affinityNode), "/document/delete/" + databaseName + "/" + collectionName + "/" + documentId, HttpMethod.DELETE);
    }

    @GetMapping("/read/{databaseName}/{collectionName}/{documentId}")
    public ResponseEntity<ApiResponse<DocumentDTO>> getDocumentById(@PathVariable String databaseName, @PathVariable String collectionName, @PathVariable String documentId) throws JsonProcessingException {
        DocumentDTO savedDocumentDTO = documentService.getDocumentById(documentId, collectionName, databaseName);
        return ResponseEntity.status(200).body(new ApiResponse<>(true, "Document retrieved successfully", savedDocumentDTO));
    }

    @GetMapping("/readByProperty/{databaseName}/{collectionName}/{property}/{value}")
    public ResponseEntity<ApiResponse<List<DocumentDTO>>> getDocumentByProperty(@PathVariable String databaseName, @PathVariable String collectionName, @PathVariable String property, @PathVariable String value) {
        List<DocumentDTO> savedDocumentDTO = documentService.getDocumentsByProperty(property, value, collectionName, databaseName);
        return ResponseEntity.status(200).body(new ApiResponse<>(true, "Document retrieved successfully", savedDocumentDTO));
    }

    @GetMapping("/readAll/{databaseName}/{collectionName}")
    public ResponseEntity<ApiResponse<List<DocumentDTO>>> getAllDocuments(@PathVariable String databaseName, @PathVariable String collectionName) {
        List<DocumentDTO> savedDocumentDTO = documentService.getAlldocuments(collectionName, databaseName);
        return ResponseEntity.status(200).body(new ApiResponse<>(true, "Document retrieved successfully", savedDocumentDTO));
    }

}