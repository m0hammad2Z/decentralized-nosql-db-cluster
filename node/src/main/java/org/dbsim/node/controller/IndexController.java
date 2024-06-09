package org.dbsim.node.controller;

import org.dbsim.node.dto.db.IndexDTO;
import org.dbsim.node.model.message.ApiResponse;
import org.dbsim.node.service.db.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/index")
public class IndexController {

    private IndexService indexService;

    @Autowired
    public IndexController(IndexService indexService) {
        this.indexService = indexService;
    }


    @PostMapping("/create")
    public ResponseEntity<ApiResponse<IndexDTO>> createIndex(@RequestBody IndexDTO indexDTO) {
        IndexDTO savedIndexDTO = indexService.saveIndex(indexDTO);
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "Index created successfully", savedIndexDTO));
    }

    @GetMapping("/get/{databaseName}/{collectionName}/{propertyName}")
    public ResponseEntity<ApiResponse<Map<String, Set<String>>>> getIndex(@PathVariable String databaseName, @PathVariable String collectionName, @PathVariable String propertyName) {
        Map<String, Set<String>> index = indexService.getIndex(databaseName, collectionName, propertyName);
        return ResponseEntity.status(200).body(new ApiResponse<>(true, "Index retrieved successfully", index));
    }

    @DeleteMapping("/delete/{databaseName}/{collectionName}/{propertyName}")
    public ResponseEntity<ApiResponse<String>> deleteIndex(@PathVariable String databaseName, @PathVariable String collectionName, @PathVariable String propertyName) {
        indexService.deleteIndex(databaseName, collectionName, propertyName);
        return ResponseEntity.status(200).body(new ApiResponse<>(true, "Index deleted successfully", null));
    }
}