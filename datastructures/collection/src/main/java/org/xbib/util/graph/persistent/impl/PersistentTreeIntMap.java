package org.xbib.util.graph.persistent.impl;

import org.xbib.util.graph.persistent.IntMap;
import org.xbib.util.persistent.PersistentTreeMap;

public class PersistentTreeIntMap<T> implements IntMap<T> {

    private final PersistentTreeMap<Integer, T> map;

    public PersistentTreeIntMap() {
        this(PersistentTreeMap.<Integer, T>empty());
    }

    public PersistentTreeIntMap(PersistentTreeMap<Integer, T> map) {
        this.map = map;
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(int key) {
        return map.containsKey(key);
    }

    @Override
    public T get(int key) {
        return map.get(key);
    }

    @Override
    public Iterable<Integer> keys() {
        return map.keys();
    }

    @Override
    public Iterable<T> values() {
        return map.values();
    }

    @Override
    public IntMap<T> put(int key, T value) {
        return new PersistentTreeIntMap<T>(map.assoc(key, value));
    }

    @Override
    public IntMap<T> remove(int key) {
        return new PersistentTreeIntMap<T>(map.dissoc(key));
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        @SuppressWarnings("unchecked") PersistentTreeIntMap<T> other = (PersistentTreeIntMap<T>) obj;
        return map.equals(other.map);
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
