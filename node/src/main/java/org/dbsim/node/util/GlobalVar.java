package org.dbsim.node.util;

import java.io.IOException;
import java.util.Properties;

public class GlobalVar {
    public static String SECRET;
    public static long EXPIRATION_TIME = 864_000_000;
    public static String HEADER_STRING = "Authorization";
    public static String DB_ROOT_DIR;
    public static int PORT;
    public static String HOSTNAME;
    public static String BOOTSTRAPPING_SERVER;
    public static int BOOTSTRAPPING_PORT;
    public static int BROADCASTING_LISTENER_PORT;

    static {
        Properties properties = new Properties();
        try {
            properties.load(GlobalVar.class.getResourceAsStream("/application.properties"));
            SECRET = System.getenv("JWT_SECRET") != null ? System.getenv("JWT_SECRET") : properties.getProperty("JWT_SECRET").replace("\"", "");
            DB_ROOT_DIR = System.getenv("DBSIM_ROOT_DIRECTORY") != null ? System.getenv("DBSIM_ROOT_DIRECTORY") : properties.getProperty("DBSIM_ROOT_DIRECTORY").replace("\"", "");
            PORT = System.getenv("server.port") != null ? Integer.parseInt(System.getenv("server.port")) : Integer.parseInt(properties.getProperty("server.port").replace("\"", ""));
            HOSTNAME = System.getenv("server.hostname") != null ? System.getenv("server.hostname") : properties.getProperty("server.hostname").replace("\"", "");
            BOOTSTRAPPING_SERVER = System.getenv("bootstrapping-server") != null ? System.getenv("bootstrapping-server") : properties.getProperty("bootstrapping-server").replace("\"", "");
            BOOTSTRAPPING_PORT = System.getenv("bootstrapping-port") != null ? Integer.parseInt(System.getenv("bootstrapping-port")) : Integer.parseInt(properties.getProperty("bootstrapping-port").replace("\"", ""));
            BROADCASTING_LISTENER_PORT = System.getenv("broadcasting-listener-port") != null ? Integer.parseInt(System.getenv("broadcasting-listener-port")) : Integer.parseInt(properties.getProperty("broadcasting-listener-port").replace("\"", ""));
        } catch (IOException e) {
            throw new RuntimeException("Could not load properties file", e);
        }
    }
}