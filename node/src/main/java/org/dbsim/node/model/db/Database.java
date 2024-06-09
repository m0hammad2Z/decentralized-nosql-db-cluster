package org.dbsim.node.model.db;

import org.dbsim.node.model.db.MetaData;

import java.io.Serializable;

public class Database implements Serializable{
    private String name;
    public Database() {
    }

    public Database(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

}
