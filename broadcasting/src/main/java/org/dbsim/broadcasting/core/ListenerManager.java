package org.dbsim.broadcasting.core;

import org.dbsim.broadcasting.api.*;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ListenerManager {
    private final ConcurrentHashMap<Topic, List<Listener>> listeners = new ConcurrentHashMap<>();
    private final String packageName;

    public ListenerManager(String packageName) {
        this.packageName = packageName;
    }

    private synchronized void addListener(Listener listener, Topic topic) throws Exception {
        listeners.putIfAbsent(topic, new ArrayList<>());
        listeners.get(topic).add(listener);
    }

    public void addListeners() throws Exception {
        ConfigurationBuilder config = new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(packageName))
                .setScanners(new MethodAnnotationsScanner());

        Reflections reflections = new Reflections(config);
        Set<Method> methods = reflections.getMethodsAnnotatedWith(BroadcastListener.class);

        Map<Class<?>, Object> classInstances = new HashMap<>();

        for (Method method : methods) {
            Class<?> clazz = method.getDeclaringClass();
            Object instance = classInstances.computeIfAbsent(clazz, k -> {
                try {
                    return k.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            BroadcastListener annotation = method.getAnnotation(BroadcastListener.class);
            Topic topic = new Topic(annotation.topic());
            Listener messageListener = message -> {
                try {
                    method.invoke(instance, message);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            };
            addListener(messageListener, topic);
        }
    }

    public synchronized void notifyListeners(Message message, Topic topic) {
        if (listeners.containsKey(topic)) {
            for (Listener listener : listeners.get(topic)) {
                listener.onMessageReceived(message);
            }
        }
    }
}