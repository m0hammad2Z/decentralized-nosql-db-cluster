package org.dbsim.node.util.btree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.Serializable;

public class BTreeNode<K extends Comparable<K>, V> implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int t;
    private K[] keys;
    private V[] values;
    private BTreeNode<K, V>[] children;
    private boolean leaf;
    private int n;

    public BTreeNode(int t) {
        this.keys = (K[]) new Comparable[2 * t - 1];
        this.values = (V[]) new Object[2 * t - 1];
        this.children = new BTreeNode[2 * t];
        this.leaf = true;
        this.n = 0;
    }

    public K[] getKeys() {
        return keys;
    }

    public void setKey(K key, int index) {
        keys[index] = key;
    }

    public BTreeNode<K, V>[] getChildren() {
        return children;
    }


    public void setChildren(BTreeNode<K, V>[] children) {
        this.children = children;
    }

    public V[] getValues() {
        return values;
    }

    public void setValue(V value, int index) {
        values[index] = value;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public void setChild(int index, BTreeNode<K, V> child) {
        children[index] = child;
    }

    public void removeKeyAndValue(int index) {
        for (int i = index; i < n - 1; i++) {
            keys[i] = keys[i + 1];
            values[i] = values[i + 1];
        }
        keys[n - 1] = null;
        values[n - 1] = null;
        n--;
    }

    public int find(K key) {
        for (int i = 0; i < n; i++) {
            if (keys[i].compareTo(key) == 0) {
                return i;
            }
        }
        return -1;
    }
}
