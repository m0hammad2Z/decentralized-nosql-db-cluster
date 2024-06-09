package org.dbsim.node.model.db;

import org.json.JSONObject;

import java.util.Optional;

public class MetaData {
    private final String fileName;
    private final JSONObject metadata;

    public MetaData(String fileName, JSONObject metadata) {
        if (fileName == null || metadata == null) {
            throw new IllegalArgumentException("FileName and metadata cannot be null");
        }
        this.fileName = fileName;
        this.metadata = metadata;

        if(!metadata.has("created")) {
            metadata.put("created", System.currentTimeMillis());
        }
    }

    public String getFileName() {
        return fileName;
    }

    public JSONObject getMetadata() {
        return metadata;
    }

    public Optional<String> getValue(String key) {
        return metadata.has(key) ? Optional.of(metadata.getString(key)) : Optional.empty();
    }

    @Override
    public String toString() {
        return metadata.toString();
    }
}