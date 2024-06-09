package org.dbsim.node.model.db;

import org.dbsim.node.util.btree.BTree;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Index implements Serializable {
    private static final long serialVersionUID = 1L;

    private String databaseName;
    private String collectionName;
    private String propertyName;
    private BTree<String, Set<String>> bTree;
    private AtomicInteger version;

    public Index() {
        this.version = new AtomicInteger(0);
        this.bTree = new BTree<>(3);
    }

    public Index(String databaseName, String collectionName, String propertyName) {
        this.databaseName = databaseName;
        this.collectionName = collectionName;
        this.propertyName = propertyName;
        this.bTree = new BTree<>(3);
        this.version = new AtomicInteger(0);
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public BTree<String, Set<String>> getBTree() {
        return bTree;
    }

    public void setBTree(BTree<String, Set<String>> bTree) {
        this.bTree = bTree;
    }

    public void setVersion(AtomicInteger version) {
        this.version = version;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public synchronized void put(String key, String value, int currentVersion) {
        checkVersion(currentVersion);
        Set<String> values = bTree.get(key);
        if (values == null) {
            values = new HashSet<>();
        }
        values.add(value);
        bTree.put(key, values);
        incrementVersion();
    }

    public synchronized Set<String> get(String key) {
        Set<String> values = bTree.get(key);
        if (values == null) {
            return new HashSet<>();
        }
        return values;
    }

    public synchronized void remove(String key, int currentVersion) {
        checkVersion(currentVersion);
        bTree.remove(key);
        incrementVersion();
    }

    public synchronized void remove(String key, String value, int currentVersion) {
        checkVersion(currentVersion);
        bTree.get(key).remove(value);
        incrementVersion();
    }
    private boolean checkVersion(int currentVersion) {
        if (currentVersion != getVersion()) {
            return false;
        }
        return true;
    }

    private int incrementVersion() {
        return version.incrementAndGet();
    }

    public int getVersion() {
        return version.get();
    }

    @Override
    public String toString() {
        return "Index{" +
                "databaseName='" + databaseName + '\'' +
                ", collectionName='" + collectionName + '\'' +
                ", propertyName='" + propertyName + '\'' +
                ", bTree=" + bTree +
                ", version=" + version +
                '}';
    }
}