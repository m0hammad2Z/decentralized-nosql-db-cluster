package com.dbsim.bootstrapping.util;

import java.io.IOException;
import java.util.Properties;

public class GlobalVar {

    public static final int BOOTSTRAP_PORT;
    public static final int BROADCASTING_LISTENER_PORT;
    public static final String BOOTSTRAP_HOSTNAME;

    static {
        Properties properties = new Properties();
        try {
            properties.load(GlobalVar.class.getResourceAsStream("/application.properties"));
            BOOTSTRAP_PORT = System.getenv("SERVER_PORT") != null ? Integer.parseInt(System.getenv("SERVER_PORT")) : Integer.parseInt(properties.getProperty("server.port").replace("\"", ""));
            BOOTSTRAP_HOSTNAME = System.getenv("SERVER_HOSTNAME") != null ? System.getenv("SERVER_HOSTNAME") : properties.getProperty("server.hostname").replace("\"", "");
            BROADCASTING_LISTENER_PORT = System.getenv("BROADCASTING_LISTENER_PORT") != null ? Integer.parseInt(System.getenv("BROADCASTING_LISTENER_PORT")) : Integer.parseInt(properties.getProperty("broadcasting-listener-port").replace("\"", ""));
        } catch (IOException e) {
            throw new RuntimeException("Could not load properties file", e);
        }
    }

}