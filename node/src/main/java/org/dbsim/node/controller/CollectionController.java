package org.dbsim.node.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.dbsim.node.dto.db.CollectionDTO;
import org.dbsim.node.model.message.ApiResponse;
import org.dbsim.node.model.node.MainNode;
import org.dbsim.node.model.node.NeighborNode;
import org.dbsim.node.service.db.CollectionService;
import org.dbsim.node.util.communication.ForwardRequestUtil;
import org.dbsim.node.util.communication.HTTPRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/collection")
public class CollectionController {

    private CollectionService collectionService;

    private ForwardRequestUtil forwardRequestUtil;
    private MainNode mainNode;

    @Autowired
    public CollectionController(CollectionService collectionService, ForwardRequestUtil forwardRequestUtil, MainNode mainNode) {
        this.collectionService = collectionService;
        this.forwardRequestUtil = forwardRequestUtil;
        this.mainNode = mainNode;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CollectionDTO>> createCollection(@RequestBody CollectionDTO collectionDTO) throws JsonProcessingException {
        int affinityNode = mainNode.getAffinityNodeId(collectionDTO.getDatabaseName() + collectionDTO.getCollectionName());
        if (affinityNode == -1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Affinity node is -1", null));
        }

        if (affinityNode == mainNode.getId()) {
            // Save the collection
            CollectionDTO savedCollectionDTO = collectionService.saveCollection(collectionDTO);
            // Return the saved collection
            return ResponseEntity.status(201).body(new ApiResponse<>(true, "Collection created successfully", savedCollectionDTO));
        }

        NeighborNode neighborNode = mainNode.getNeighborNode(affinityNode);
        return forwardRequestUtil.forward(collectionDTO, neighborNode, "/collection/create", HttpMethod.POST);
    }


    @DeleteMapping("/delete/{databaseName}/{collectionName}")
    public ResponseEntity<ApiResponse<String>> deleteCollection(@PathVariable String databaseName, @PathVariable String collectionName) throws JsonProcessingException {
        int affinityNode = mainNode.getAffinityNodeId(databaseName + collectionName);
        if (affinityNode == -1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Affinity node is -1", null));
        }
        if (affinityNode == mainNode.getId()) {
            // Delete the collection
            collectionService.deleteCollection(collectionName, databaseName);
            // Return the response
            return ResponseEntity.status(200).body(new ApiResponse<>(true, "Collection deleted successfully", null ));
        }

        NeighborNode neighborNode = mainNode.getNeighborNode(affinityNode);
        return forwardRequestUtil.forward(null, neighborNode, "/collection/delete/" + databaseName + "/" + collectionName, HttpMethod.DELETE);
    }

    @GetMapping("/get/{databaseName}/{collectionName}")
    public ResponseEntity<ApiResponse<CollectionDTO>> getCollection(@PathVariable String databaseName, @PathVariable String collectionName) {
        // Get the collection
        CollectionDTO collection = collectionService.getCollectionDetails(collectionName, databaseName);
        // Return the response
        return ResponseEntity.status(200).body(new ApiResponse<>(true, "Collection retrieved successfully", collection));
    }
}