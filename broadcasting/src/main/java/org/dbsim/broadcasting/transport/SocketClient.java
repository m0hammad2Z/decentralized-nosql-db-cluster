package org.dbsim.broadcasting.transport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbsim.broadcasting.exception.ConnectionFailedException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class SocketClient {
    private static final Logger logger = LogManager.getLogger(SocketClient.class);
    private final int batchSize;
    private final int port;
    private final String host;
    private final ExecutorService executorService;
    private final BlockingQueue<String> messageQueue;
    private final BlockingQueue<Socket> socketPool;

    public SocketClient(String host, int port, int batchSize) {
        this.host = host;
        this.port = port;
        this.socketPool = new LinkedBlockingQueue<>();
        this.messageQueue = new LinkedBlockingQueue<>();
        this.executorService = Executors.newCachedThreadPool();
        this.batchSize = batchSize;
        initSocketPool();
        startMessageSender();
    }

    private void initSocketPool() {
        for (int i = 0; i < 5; i++) {
            try {
                Socket socket = new Socket(host, port);
                socketPool.offer(socket);
            } catch (UnknownHostException e) {
                logger.error("Unknown host: " + host);
            } catch (IOException e) {
                logger.error("Failed to create socket: " + e.getMessage());
            }
        }
    }

    private Socket getSocket() {
        Socket socket = socketPool.poll();
        while (socket == null || socket.isClosed()) {
            try {
                socket = new Socket(host, port);
                break;
            } catch (IOException e) {
//                logger.error("Failed to get a socket from the pool or create a new one, attempt ");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ConnectionFailedException("Interrupted while waiting to retry socket creation");
                }
            }
        }
        return socket;
    }

    private void sendBatch() {
        List<String> batch = new ArrayList<>(batchSize);
        messageQueue.drainTo(batch, batchSize);
        String batchMessage = String.join("\n", batch);
        executorService.submit(() -> sendInternal(batchMessage));
    }

    private void startMessageSender() {
        executorService.submit(() -> {
            int timer = 2;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (messageQueue.size() >= batchSize) {
                        sendBatch();
                    }else if (timer == 0) {
                        sendBatch();
                        timer = 5;
                    } else {
                        timer--;
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    public void send(String message) {
        messageQueue.offer(message);
    }

    private void sendInternal(String message) {
        try {
            Socket socket = getSocket();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                writer.write(message);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                logger.error("Error sending message: " + e.getMessage());
                closeSocket(socket);
            } finally {
                if (!socket.isClosed()) {
                    socketPool.offer(socket);
                }
            }
        } catch (ConnectionFailedException e) {
            logger.error("Failed to send message: " + e.getMessage());
        }
    }

    private void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            logger.error("Error closing socket: " + e.getMessage(), e);
        }
    }

    public synchronized void close() {
        executorService.shutdownNow();
        socketPool.forEach(this::closeSocket);
        socketPool.clear();
    }

    public boolean equals(int port, String host) {
        return this.port == port && this.host.equals(host);
    }

    @Override
    public String toString() {
        return "{" +
                ", port=" + port +
                ", host='" + host + '\'' +
                '}';
    }
}