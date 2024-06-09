package org.dbsim.node.service.db;

import org.dbsim.node.dto.db.IndexDTO;
import org.dbsim.node.enums.DBEventType;
import org.dbsim.node.model.db.Index;
import org.dbsim.node.repository.IndexRepository;
import org.dbsim.node.service.node.service.DBGossipService;
import org.dbsim.node.util.validation.Validator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class IndexService {

    private IndexRepository indexRepository;
    private DBGossipService dbGossipService;
    private ModelMapper modelMapper;

    @Autowired
    public IndexService(IndexRepository indexRepository, DBGossipService dbGossipService, ModelMapper modelMapper) {
        this.indexRepository = indexRepository;
        this.dbGossipService = dbGossipService;
        this.modelMapper = modelMapper;
    }

    public IndexDTO saveIndex(IndexDTO indexDTO) {
        Index index = modelMapper.map(indexDTO, Index.class);

        Validator.isNotNull(index, "Index must not be null", true);
        Validator.isValidString(index.getDatabaseName(), "Database name must not be null or empty", true);
        Validator.isValidString(index.getCollectionName(), "Collection name must not be null or empty", true);
        Validator.isValidString(index.getPropertyName(), "Property name must not be null or empty", true);

        indexRepository.saveIndex(index);

        dbGossipService.addEvent(DBEventType.INDEX_CREATED, Map.of("databaseName", index.getDatabaseName(), "collectionName", index.getCollectionName(), "propertyName", index.getPropertyName()));

        return modelMapper.map(index, IndexDTO.class);
    }


    public Map<String, Set<String>> getIndex(String databaseName, String collectionName, String propertyName) {
        Validator.isValidString(databaseName, "Database name must not be null or empty", true);
        Validator.isValidString(collectionName, "Collection name must not be null or empty", true);
        Validator.isValidString(propertyName, "Property name must not be null or empty", true);

        return indexRepository.findIndexByProperty(propertyName, collectionName, databaseName).getBTree().toMap();
    }

    public List<Index> getIndexesByCollection(String databaseName, String collectionName) {
        Validator.isValidString(databaseName, "Database name must not be null or empty", true);
        Validator.isValidString(collectionName, "Collection name must not be null or empty", true);

        return indexRepository.findIndexByCollection(collectionName, databaseName);
    }

    public void deleteIndex(String databaseName, String collectionName, String propertyName) {
        Validator.isValidString(databaseName, "Database name must not be null or empty", true);
        Validator.isValidString(collectionName, "Collection name must not be null or empty", true);
        Validator.isValidString(propertyName, "Property name must not be null or empty", true);

        indexRepository.deleteIndex(databaseName, collectionName, propertyName);

        dbGossipService.addEvent(DBEventType.INDEX_DELETED, Map.of("databaseName", databaseName, "collectionName", collectionName, "propertyName", propertyName));

    }

    public void put(String databaseName, String collectionName, String propertyName, String key, String value) {
        Index index = indexRepository.findIndexByProperty(propertyName, collectionName, databaseName);
        index.put(key, value, index.getVersion());
        indexRepository.saveIndex(index);
    }

    public void remove(String databaseName, String collectionName, String propertyName, String key, String value) {
        Index existingIndex = indexRepository.findIndexByProperty(propertyName, collectionName, databaseName);
        existingIndex.remove(key, value, existingIndex.getVersion());
        indexRepository.saveIndex(existingIndex);
    }
}