package org.dbsim.node.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.dbsim.node.dto.db.DatabaseDTO;
import org.dbsim.node.model.message.ApiResponse;
import org.dbsim.node.model.node.MainNode;
import org.dbsim.node.model.node.NeighborNode;
import org.dbsim.node.service.db.DatabaseService;
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
@RequestMapping("/database")
public class DataBaseController {
    private DatabaseService databaseService;

    private ForwardRequestUtil forwardRequestUtil;
    private MainNode mainNode;

    @Autowired
    public DataBaseController(DatabaseService databaseService, ForwardRequestUtil forwardRequestUtil, MainNode mainNode) {
        this.databaseService = databaseService;
        this.forwardRequestUtil = forwardRequestUtil;
        this.mainNode = mainNode;
    }


    @PostMapping("/create")
    public ResponseEntity<ApiResponse<DatabaseDTO>> createDatabase(@Valid @RequestBody DatabaseDTO databaseDTO) throws JsonProcessingException {
        int affinityNode = mainNode.getAffinityNodeId(databaseDTO.getName());
        if (affinityNode == -1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Affinity node is -1", null));
        }
        if (affinityNode == mainNode.getId()) {
            // Save the database
            DatabaseDTO savedDatabaseDTO = databaseService.saveDatabase(databaseDTO);
            // Return the saved database
            return ResponseEntity.status(201).body(new ApiResponse<>(true, "Database created successfully", savedDatabaseDTO));
        }

        NeighborNode neighborNode = mainNode.getNeighborNode(affinityNode);
        return forwardRequestUtil.forward(databaseDTO, neighborNode, "/database/create", HttpMethod.POST);
    }

    @DeleteMapping("/delete/{databaseName}")
    public ResponseEntity<ApiResponse<String>> deleteDatabase(@PathVariable String databaseName) throws JsonProcessingException {
        int affinityNode = mainNode.getAffinityNodeId(databaseName);
        if (affinityNode == -1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Affinity node is -1", null));
        }

        if (affinityNode == mainNode.getId()) {
            // Delete the database
            databaseService.deleteDatabase(databaseName);
            // Return the success message
            return ResponseEntity.status(200).body(new ApiResponse<>(true, "Database deleted successfully", null));
        }

        NeighborNode neighborNode = mainNode.getNeighborNode(affinityNode);
        return forwardRequestUtil.forward(null, neighborNode, "/database/delete/" + databaseName, HttpMethod.DELETE);
    }
}