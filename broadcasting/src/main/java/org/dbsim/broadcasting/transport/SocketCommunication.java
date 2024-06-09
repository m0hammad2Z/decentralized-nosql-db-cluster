package org.dbsim.broadcasting.transport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbsim.broadcasting.api.Communication;
import org.dbsim.broadcasting.api.Message;
import org.dbsim.broadcasting.api.Topic;
import org.dbsim.broadcasting.core.ListenerManager;
import org.dbsim.broadcasting.exception.ConnectionFailedException;
import org.dbsim.broadcasting.util.JsonConverter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketCommunication implements Communication{
    private final int port;
    private final int batchSize;
    private ServerSocket serverSocket;
    private final ListenerManager listenerManager;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private volatile boolean isRunning = false;
    private static final Logger logger = LogManager.getLogger(SocketCommunication.class);
    public SocketCommunication(int port, String listenerPackage, int batchSize) {
        this.batchSize = batchSize;
        this.port = port;
        this.listenerManager = new ListenerManager(listenerPackage);
        start();
    }

    @Override
    public synchronized void start() throws ConnectionFailedException {
        try {
            listenerManager.addListeners();
            serverSocket = new ServerSocket(port);
            isRunning = true;
            executorService.submit(this);
            logger.info("Server started on port: " + port);
        } catch (Exception e) {
            logger.error("Failed to start server on port: " + port + " " + e.getMessage(), e);
            throw new ConnectionFailedException("Failed to start server on port: " + port + " " + e.getMessage());
        }
    }


    @Override
    public void run() {
        if (serverSocket == null) {
            logger.error("Server socket is null");
            return;
        }

        List<Message> messageBatch = new ArrayList<>(batchSize);
        List<Topic> topicBatch = new ArrayList<>(batchSize);

        while (isRunning) {
            try (Socket socket = serverSocket.accept();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                String json = reader.readLine();
                if (json == null || json.isEmpty()) {
                    continue;
                }

                Map map = JsonConverter.convertFromJSON(json, Map.class);

                Object messageJson = map.get("message");
                Object topicJson = map.get("topic");

                Message messageObject = JsonConverter.convertFromJSON(messageJson.toString(), Message.class);
                Topic topic = JsonConverter.convertFromJSON(topicJson.toString(), Topic.class);

                messageBatch.add(messageObject);
                topicBatch.add(topic);

                if (messageBatch.size() >= batchSize) {
                    for (int i = 0; i < batchSize; i++) {
                        listenerManager.notifyListeners(messageBatch.get(i), topicBatch.get(i));
                    }
                    messageBatch.clear();
                    topicBatch.clear();
                }
            } catch (SocketException e) {
                logger.error("Client closed the connection: " + e.getMessage(), e);
            } catch (IOException e) {
                logger.error("Failed to read message: " + e.getMessage(), e);
                isRunning = false;
            }
        }

        // Process remaining messages in the batch
        for (int i = 0; i < messageBatch.size(); i++) {
            listenerManager.notifyListeners(messageBatch.get(i), topicBatch.get(i));
        }
    }



    private void disconnect() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.error("Failed to close server socket: " + e.getMessage(), e);
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void shutdown() {
        isRunning = false;
        executorService.shutdown();
        disconnect();
    }
}