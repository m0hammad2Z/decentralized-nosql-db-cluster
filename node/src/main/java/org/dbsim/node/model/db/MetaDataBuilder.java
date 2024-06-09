package org.dbsim.node.model.db;

import org.dbsim.node.model.db.MetaData;
import org.json.JSONObject;

public class MetaDataBuilder {
    private final MetaData metaData;

    private MetaDataBuilder(String fileName, JSONObject metadata) {
        this.metaData = new MetaData(fileName, metadata);
    }

    public static MetaDataBuilder createMetaData(String fileName) {
        return new MetaDataBuilder(fileName, new JSONObject());
    }

    public static MetaDataBuilder createMetaData(String fileName, JSONObject metadata) {
        return new MetaDataBuilder(fileName, metadata);
    }

    // Add data to the metadata
    public MetaDataBuilder addData(String key, String value) {
        JSONObject newMetadata = new JSONObject(metaData.getMetadata().toMap());
        newMetadata.put(key, value);
        return new MetaDataBuilder(metaData.getFileName(), newMetadata);
    }

    public MetaData build() {
        return metaData;
    }
}