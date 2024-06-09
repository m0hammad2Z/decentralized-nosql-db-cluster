package org.dbsim.node.service.db;

import org.dbsim.node.dto.db.DatabaseDTO;
import org.dbsim.node.enums.DBEventType;
import org.dbsim.node.model.db.Database;
import org.dbsim.node.repository.DatabaseRepository;
import org.dbsim.node.service.node.service.DBGossipService;
import org.dbsim.node.util.validation.Validator;
import org.dbsim.node.model.db.MetaDataBuilder;
import org.dbsim.node.model.db.MetaData;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
public class DatabaseService {


    private DatabaseRepository databaseRepository;
    private DBGossipService dbGossipService;
    private ModelMapper modelMapper;

    @Autowired
    public DatabaseService(DatabaseRepository databaseRepository, DBGossipService dbGossipService, ModelMapper modelMapper) {
        this.databaseRepository = databaseRepository;
        this.dbGossipService = dbGossipService;
        this.modelMapper = modelMapper;
    }


    public DatabaseDTO saveDatabase(DatabaseDTO databaseDTO) {
        // Convert DTO to entity
        Database database = modelMapper.map(databaseDTO, Database.class);

        // Validation
        Validator.isNotNull(database, "Database must not be null", true);
        Validator.isValidString(database.getName(), "Database name must not be null or empty", true);

        // Create metadata
        MetaData metaData = buildMetaData(database.getName());

        // Save database
        databaseRepository.saveDatabase(database, metaData);

        // Add event to gossip
        dbGossipService.addEvent(DBEventType.DATABASE_CREATED, databaseDTO);

        return modelMapper.map(database, DatabaseDTO.class);
    }

    public void deleteDatabase(String databaseName) {
        // Validation
        Validator.isValidString(databaseName, "Database name must not be null or empty", true);

        // Delete database
        databaseRepository.deleteDatabase(databaseName);

        // Add event to gossip
        dbGossipService.addEvent(DBEventType.DATABASE_DELETED, databaseName);
    }

    private MetaData buildMetaData(String databaseName) {
        return MetaDataBuilder.createMetaData(databaseName)
                .addData("databaseName", databaseName)
                .addData("created", Date.from(Instant.now()).toString())
                .build();
    }
}
