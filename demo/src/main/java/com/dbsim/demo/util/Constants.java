package com.dbsim.demo.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Constants {

    public static final String BOOTSTRAPPING_NODE_SERVER;
    public static final String BOOTSTRAPPING_NODE_PORT;

    static {
        Properties properties = new Properties();
        try {
            properties.load(Constants.class.getResourceAsStream("/application.properties"));
            BOOTSTRAPPING_NODE_SERVER = System.getenv("bootstrapping-node-server") != null ? System.getenv("bootstrapping-node-server") : properties.getProperty("bootstrapping-node-server").replace("\"", "");
            BOOTSTRAPPING_NODE_PORT = System.getenv("bootstrapping-node-port") != null ? System.getenv("bootstrapping-node-port") : properties.getProperty("bootstrapping-node-port").replace("\"", "");
        } catch (IOException e) {
            throw new RuntimeException("Could not load properties file", e);
        }
    }

}
