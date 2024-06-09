package org.dbsim.broadcasting;

import org.dbsim.broadcasting.api.BroadcastMechanism;
import org.dbsim.broadcasting.api.Message;
import org.dbsim.broadcasting.api.Topic;
import org.dbsim.broadcasting.core.bmechanism.BroadcastToAll;
import org.dbsim.broadcasting.core.Broadcaster;
import org.dbsim.broadcasting.transport.SocketCommunication;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main(String[] args) throws IOException, InterruptedException {

//        new SocketCommunication(9999, "org.dbsim.broadcasting", 5);
//
//        Broadcaster broadcaster = new Broadcaster(5);
//        broadcaster.addClient("localhost", 2222);
//
//        Topic topic = new Topic("heartbeat");
//
//        // Create an ExecutorService with a fixed thread pool
//        ExecutorService executorService = Executors.newFixedThreadPool(10);
//
//        try {
//            TimeUnit.SECONDS.sleep(1);
//            broadcaster.addClient("localhost", 1111);
//        } catch (InterruptedException e) {
//        }
//
//        // Create a Runnable task that sends a message
//        Runnable task = () -> {
//            for (int i = 0; i < 2000; i++) {
//                Message message = new Message(i);
//                try {
//                    broadcaster.broadcast("broadcastToAll", message, topic);
//                } catch (Exception e) {
//
//                }
//
//                try {
//                    Thread.sleep(Math.round(Math.random() * 700));
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        };
//
//        // Submit the task to the ExecutorService multiple times
//        for (int i = 0; i < 10; i++) {
//            executorService.submit(task);
//        }
//
//        // Shutdown the ExecutorService
//        executorService.shutdown();
    }
}
