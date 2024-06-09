package org.dbsim.node.dto.db;

import org.dbsim.node.model.db.MetaData;

import javax.validation.constraints.NotBlank;

public class DatabaseDTO {

    private String name;

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
