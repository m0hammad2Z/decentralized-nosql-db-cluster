package org.dbsim.node.service.db;
import org.dbsim.node.dto.db.CollectionDTO;
import org.dbsim.node.enums.DBEventType;
import org.dbsim.node.model.db.Collection;
import org.dbsim.node.repository.CollectionRepository;
import org.dbsim.node.service.node.service.DBGossipService;
import org.dbsim.node.util.validation.Validator;
import org.dbsim.node.model.db.MetaDataBuilder;
import org.dbsim.node.model.db.MetaData;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class CollectionService {


    private CollectionRepository collectionRepository;
    private DBGossipService dbGossipService;

    private ModelMapper modelMapper;

    @Autowired
    public CollectionService(CollectionRepository collectionRepository, DBGossipService dbGossipService, ModelMapper modelMapper) {
        this.collectionRepository = collectionRepository;
        this.dbGossipService = dbGossipService;
        this.modelMapper = modelMapper;
    }

    public CollectionDTO saveCollection(CollectionDTO collectionDTO) {
        Collection collection = modelMapper.map(collectionDTO, Collection.class);
        Validator.isNotNull(collection, "Collection must not be null", true);
        Validator.isValidString(collection.getCollectionName(), "Collection name must not be null or empty", true);
        Validator.isValidString(collection.getDatabaseName(), "Database name must not be null or empty", true);

        JSONObject schema = new JSONObject(collection.getSchema());
        Validator.isValidSchema(schema, true);


        MetaData metaData = buildMetaData(collection.getDatabaseName(), collection.getCollectionName(), schema);

        collectionRepository.saveCollection(collection, metaData);

        dbGossipService.addEvent(DBEventType.COLLECTION_CREATED, collectionDTO);

        return modelMapper.map(collection, CollectionDTO.class);
    }

    public void deleteCollection(String collectionName, String databaseName) {
        Validator.isValidString(databaseName, "Database name must not be null or empty", true);
        Validator.isValidString(collectionName, "Collection name must not be null or empty", true);

        collectionRepository.deleteCollection(collectionName,databaseName);

        dbGossipService.addEvent(DBEventType.COLLECTION_DELETED, Map.of("collectionName", collectionName, "databaseName", databaseName));
    }


    public CollectionDTO getCollectionDetails(String collectionName, String databaseName) {
        Validator.isValidString(databaseName, "Database name must not be null or empty", true);
        Validator.isValidString(collectionName, "Collection name must not be null or empty", true);

        return modelMapper.map(collectionRepository.getCollectionDetails(databaseName,collectionName), CollectionDTO.class);
    }


    private MetaData buildMetaData(String databaseName, String collectionName, JSONObject schema) {
        return MetaDataBuilder.createMetaData(collectionName)
                .addData("collectionName", collectionName)
                .addData("databaseName", databaseName)
                .addData("created", Date.from(Instant.now()).toString())
                .addData("schema", schema.toString())
                .build();
    }

}
