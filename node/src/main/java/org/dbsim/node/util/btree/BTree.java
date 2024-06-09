package org.dbsim.node.util.btree;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BTree<K extends Comparable<K>, V> implements Serializable{

    private static final long serialVersionUID = 1L;
    private BTreeNode<K, V> root;
    private int t; // Minimum degree

    public BTree(int t) {
        this.t = t;
        root = new BTreeNode<>(t);
    }

    public void put(K key, V value) {
        if(contains(key)) {
            return;
        }
        BTreeNode<K, V> r = root;
        if (r.getN() == 2 * t - 1) {
            BTreeNode<K, V> s = new BTreeNode<>(t);
            root = s;
            s.setLeaf(false);
            s.setN(0);
            s.getChildren()[0] = r;
            splitChild(s, 0, r);
            insertNonFull(s, key, value);
        } else {
            insertNonFull(r, key, value);
        }
    }

    private void splitChild(BTreeNode<K, V> x, int i, BTreeNode<K,V> y){
        BTreeNode<K, V> z = new BTreeNode<>( t);
        z.setLeaf(y.isLeaf());
        z.setN(t - 1);
        for(int j = 0; j < t - 1; j++){
            z.getKeys()[j] = y.getKeys()[j + t];
            z.getValues()[j] = y.getValues()[j + t];
        }
        if(!y.isLeaf()){
            for(int j = 0; j < t; j++){
                z.getChildren()[j] = y.getChildren()[j + t];
            }
        }
        y.setN(t - 1);
        for(int j = x.getN(); j >= i + 1; j--){
            x.getChildren()[j + 1] = x.getChildren()[j];
        }
        x.getChildren()[i + 1] = z;
        for(int j = x.getN() - 1; j >= i; j--){
            x.getKeys()[j + 1] = x.getKeys()[j];
            x.getValues()[j + 1] = x.getValues()[j];
        }
        x.getKeys()[i] = y.getKeys()[t - 1];
        x.getValues()[i] = y.getValues()[t - 1];
        x.setN(x.getN() + 1);
    }

    private void insertNonFull(BTreeNode<K, V> x, K k, V v) {
        if(x.isLeaf()){
            int i =0;
            for(i = x.getN() -1; i>= 0 && k.compareTo(x.getKeys()[i]) < 0; i--){
                x.setKey(x.getKeys()[i], i+1);
                x.setValue(x.getValues()[i], i+1);
            }
            x.setKey(k, i+1);
            x.setValue(v, i+1);
            x.setN(x.getN() + 1);
        } else {
            int i = 0;
            for(i = x.getN() -1; i>= 0 && x.getKeys()[i].compareTo(k) > 0; i--);
            i++;
            BTreeNode<K, V> temp = x.getChildren()[i];
            if(temp.getN() == 2 * t - 1){
                splitChild(x, i, temp);
                if(k.compareTo(x.getKeys()[i]) > 0){
                    i++;
                }
            }
            insertNonFull(x.getChildren()[i], k, v);
        }
    }

    public void update(K key, V value) {
        if(!contains(key)) {
            return;
        }
        BTreeNode<K, V> r = root;
        BTreeNode<K, V> x = get(r, key);
        if(x == null) {
            return;
        }
        int i = 0;
        for(i = 0; i < x.getN(); i++) {
            if(key.compareTo(x.getKeys()[i]) == 0) {
                break;
            }
        }
        x.setValue(value, i);

    }

    public V get(K key) {
        if(!contains(key)) {
            return null;
        }
        BTreeNode<K, V> r = root;
        BTreeNode<K, V> x = get(r, key);
        if(x == null) {
            return null;
        }
        int i = 0;
        for(i = 0; i < x.getN(); i++) {
            if(key.compareTo(x.getKeys()[i]) == 0) {
                break;
            }
        }
        return x.getValues()[i];
    }

    private BTreeNode<K,V> get(BTreeNode<K, V> x, K key){
        int i = 0;
        if(x == null) {
            return null;
        }
        for(i = 0; i < x.getN(); i++) {
            if(key.compareTo(x.getKeys()[i]) < 0) {
                break;
            }
            if(key.compareTo(x.getKeys()[i]) == 0) {
                return x;
            }
        }
        if(x.isLeaf()) {
            return null;
        } else {
            return get(x.getChildren()[i], key);
        }
    }

    public boolean contains(K key) {
        if(root == null) {
            return false;
        }
        return get(root, key) != null;
    }

    public void remove(K key) {
        remove(root, key);
        if (root.getN() == 0) {
            BTreeNode<K, V> tmp = root;
            root = root.getChildren()[0];
        }
    }

    private void remove(BTreeNode<K, V> x, K key) {
        int pos = x.find(key);

        if (pos != -1) {
            if (x.isLeaf()) {
                x.removeKeyAndValue(pos);
                return;
            }

            BTreeNode<K, V> pred = x.getChildren()[pos];
            BTreeNode<K, V> succ = x.getChildren()[pos + 1];

            if (pred.getN() >= t) {
                K predKey = pred.getKeys()[pred.getN() - 1];
                remove(pred, predKey);
                x.setKey(predKey, pos);
                return;
            }

            if (succ.getN() >= t) {
                K succKey = succ.getKeys()[0];
                remove(succ, succKey);
                x.setKey(succKey, pos);
                return;
            }

            merge(x, pos);
            remove(pred, key);
        } else {
            for (pos = 0; pos < x.getN(); pos++) {
                if (x.getKeys()[pos].compareTo(key) > 0) {
                    break;
                }
            }

            BTreeNode<K, V> tmp = x.getChildren()[pos];

            if (tmp.getN() < t) {
                if (pos < x.getN() && x.getChildren()[pos + 1].getN() >= t) {
                    shiftLeft(x, pos);
                } else if (pos > 0 && x.getChildren()[pos - 1].getN() >= t) {
                    shiftRight(x, pos);
                } else {
                    if (pos < x.getN()) {
                        merge(x, pos);
                    } else {
                        merge(x, pos - 1);
                    }
                }
            }

            remove(x.getChildren()[pos], key);
        }
    }

    private void merge(BTreeNode<K, V> x, int i) {
        BTreeNode<K, V> child = x.getChildren()[i];
        BTreeNode<K, V> sibling = x.getChildren()[i + 1];

        child.getKeys()[t - 1] = x.getKeys()[i];
        child.getValues()[t - 1] = x.getValues()[i];

        for (int j = 0; j < sibling.getN(); j++) {
            child.getKeys()[j + t] = sibling.getKeys()[j];
            child.getValues()[j + t] = sibling.getValues()[j];
        }

        if (!child.isLeaf()) {
            for (int j = 0; j <= sibling.getN(); j++) {
                child.getChildren()[j + t] = sibling.getChildren()[j];
            }
        }

        for (int j = i; j < x.getN() - 1; j++) {
            x.getKeys()[j] = x.getKeys()[j + 1];
            x.getValues()[j] = x.getValues()[j + 1];
            x.getChildren()[j + 1] = x.getChildren()[j + 2];
        }

        child.setN(child.getN() + sibling.getN() + 1);
        x.setN(x.getN() - 1);
    }

    private void shiftRight(BTreeNode<K, V> x, int i) {
        BTreeNode<K, V> y = x.getChildren()[i];
        BTreeNode<K, V> z = x.getChildren()[i - 1];

        for (int j = y.getN(); j > 0; j--) {
            y.getKeys()[j] = y.getKeys()[j - 1];
            y.getValues()[j] = y.getValues()[j - 1];
        }
        y.getKeys()[0] = x.getKeys()[i - 1];
        y.getValues()[0] = x.getValues()[i - 1];

        if (!y.isLeaf()) {
            for (int j = y.getN() + 1; j > 0; j--) {
                y.getChildren()[j] = y.getChildren()[j - 1];
            }
            y.getChildren()[0] = z.getChildren()[z.getN()];
        }

        x.getKeys()[i - 1] = z.getKeys()[z.getN() - 1];
        x.getValues()[i - 1] = z.getValues()[z.getN() - 1];

        y.setN(y.getN() + 1);
        z.setN(z.getN() - 1);
    }

    private void shiftLeft(BTreeNode<K, V> x, int i) {
        BTreeNode<K, V> y = x.getChildren()[i];
        BTreeNode<K, V> z = x.getChildren()[i + 1];

        y.getKeys()[y.getN()] = x.getKeys()[i];
        y.getValues()[y.getN()] = x.getValues()[i];

        if (!y.isLeaf()) {
            y.getChildren()[y.getN() + 1] = z.getChildren()[0];
        }

        x.getKeys()[i] = z.getKeys()[0];
        x.getValues()[i] = z.getValues()[0];

        for (int j = 0; j < z.getN() - 1; j++) {
            z.getKeys()[j] = z.getKeys()[j + 1];
            z.getValues()[j] = z.getValues()[j + 1];
        }

        if (!z.isLeaf()) {
            for (int j = 0; j < z.getN(); j++) {
                z.getChildren()[j] = z.getChildren()[j + 1];
            }
        }

        y.setN(y.getN() + 1);
        z.setN(z.getN() - 1);
    }

    public int size() {
        return size(root);
    }

    private int size(BTreeNode<K, V> node) {
        if (node == null) {
            return 0;
        }
        int size = node.getN();
        for (int i = 0; i <= node.getN(); i++) {
            size += size(node.getChildren()[i]);
        }
        return size;
    }

    public Map<K, V> toMap() {
        return toMap(root);
    }

    private Map<K, V> toMap(BTreeNode<K, V> node) {
        if (node == null) {
            return null;
        }
        Map<K, V> map = new ConcurrentHashMap<>();
        for (int i = 0; i < node.getN(); i++) {
            map.put(node.getKeys()[i], node.getValues()[i]);
        }
        for (int i = 0; i <= node.getN(); i++) {
            Map<K, V> childMap = toMap(node.getChildren()[i]);
            if (childMap != null) {
                map.putAll(childMap);
            }
        }
        return map;
    }

    @Override
    public String toString() {
        return toString(root, 0);
    }

    private String toString(BTreeNode<K, V> node, int level) {
        StringBuilder builder = new StringBuilder();

        for (int i = node.getN() - 1; i >= 0; i--) {
            if (!node.isLeaf()) {
                builder.append(toString(node.getChildren()[i + 1], level + 1));
            }

            for (int j = 0; j < level; j++) {
                builder.append("\t\t");
            }

            builder.append(node.getKeys()[i]).append("\n");

            if (!node.isLeaf() && i == 0) {
                builder.append(toString(node.getChildren()[i], level + 1));
            }
        }

        return builder.toString();
    }


    public void putAll(BTree<K, V> bTree) {
        putAll(root, bTree.root);
    }

    private void putAll(BTreeNode<K, V> node, BTreeNode<K, V> otherNode) {
        if (otherNode == null) {
            return;
        }
        for (int i = 0; i < otherNode.getN(); i++) {
            put(otherNode.getKeys()[i], otherNode.getValues()[i]);
        }
        for (int i = 0; i <= otherNode.getN(); i++) {
            putAll(node.getChildren()[i], otherNode.getChildren()[i]);
        }
    }

}