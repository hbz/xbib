package org.xbib.util.persistent;

import org.xbib.util.persistent.AbstractHashMap.EntryNode;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractHashMap<K, V, This extends AbstractHashMap<K, V, This>>
        extends AbstractHashTrie<K, EntryNode<K, V>, AbstractHashMap<K, V, This>>
        implements Iterable<Map.Entry<K, V>> {

    @SuppressWarnings("rawtypes")
    private static final Function TO_ENTRY = (Object input) -> toEntry((Map.Entry) input);

    @SuppressWarnings("unchecked")
    protected static <K, V> EntryNode<K, V> toEntry(Map.Entry<? extends K, ? extends V> entry) {
        if (entry instanceof EntryNode) {
            return (EntryNode<K, V>) entry;
        } else {
            return new EntryNode<>(entry.getKey(), entry.getValue());
        }
    }

    public This assoc(K key, V value) {
        return assoc(new EntryNode<K, V>(key, value));
    }

    private This assoc(java.util.Map.Entry<? extends K, ? extends V> entry) {
        return merge(entry, null);
    }

    public This assocAll(Map<? extends K, ? extends V> map) {
        return mergeAll(map, null);
    }

    public This assocAll(Iterable<Map.Entry<K, V>> entries) {
        return mergeAll(entries, null);
    }

    public This merge(K key, V value, Merger<Map.Entry<K, V>> merger) {
        return doMerge(new EntryNode<K, V>(key, value), merger);
    }

    public This merge(Map.Entry<? extends K, ? extends V> entry, Merger<Map.Entry<K, V>> merger) {
        return doMerge(toEntry(entry), merger);
    }

    @SuppressWarnings("unchecked")
    protected This doMerge(EntryNode<K, V> entry, Merger<Map.Entry<K, V>> merger) {
        final UpdateContext<Map.Entry<K, V>> updateContext = updateContext(1, merger);
        return (This) doAdd(updateContext, toEntry(entry));
    }

    @SuppressWarnings("unchecked")
    public This mergeAll(Map<? extends K, ? extends V> map, Merger<Map.Entry<K, V>> merger) {
        final UpdateContext<Map.Entry<K, V>> updateContext = updateContext(map.size(), merger);
        return (This) doAddAll(updateContext, TransformedIterator.transform(map.entrySet().iterator(), TO_ENTRY));
    }

    @SuppressWarnings("unchecked")
    public This mergeAll(Iterable<Map.Entry<K, V>> entries, Merger<Map.Entry<K, V>> merger) {
        final UpdateContext<Map.Entry<K, V>> updateContext = updateContext(32, merger);
        return (This) doAddAll(updateContext, TransformedIterator.transform(entries.iterator(), TO_ENTRY));
    }

    protected UpdateContext<Map.Entry<K, V>> updateContext(int expectedSize, Merger<Map.Entry<K, V>> merger) {
        return new UpdateContext<>(expectedSize, merger);
    }

    public This dissoc(Object key) {
        return dissoc(key, null);
    }

    @SuppressWarnings("unchecked")
    public This dissoc(Object key, Merger<Map.Entry<K, V>> merger) {
        final UpdateContext<Map.Entry<K, V>> updateContext = updateContext(1, merger);
        return (This) doRemove(updateContext, key);
    }

    public V get(Object key) {
        EntryNode<K, V> entry = root().find(key);
        return entry != null ? entry.getValue() : null;
    }

    public boolean containsKey(Object key) {
        return root().find(key) != null;
    }

    public Iterable<K> keys() {
        return FluentIterable.transform(this, MapUtils.<K>mapKeyFunction());
    }

    public Iterable<V> values() {
        return FluentIterable.transform(this, MapUtils.<V>mapValueFunction());
    }

    public Iterator<Map.Entry<K, V>> iterator() {
        return TransformedIterator.transform(doIterator(), MapUtils.<K, V>mapEntryFunction());
    }

    public static final class EntryNode<K, V> extends AbstractHashTrie.EntryNode<K, EntryNode<K, V>> implements Map.Entry<K, V> {

        final V value;

        public EntryNode(K key, V value) {
            super(key);
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        public String toString() {
            return "" + key + ": " + value;
        }

        @Override
        public Node<K, EntryNode<K, V>> assocInternal(final UpdateContext<? super EntryNode<K, V>> currentContext, final int shift, final int hash, final EntryNode<K, V> newEntry) {
            if (Objects.equals(key, newEntry.key)) {
                return currentContext.merge(this, newEntry) ? newEntry : this;
            } else {
                return split(currentContext, shift, hash, newEntry);
            }
        }
    }

    static class EntrySpliterator<K, V> extends NodeSpliterator<Map.Entry<K, V>, K, EntryNode<K, V>> {

        public EntrySpliterator(Node<K, EntryNode<K, V>> node, int sizeEstimate, boolean immutable) {
            super(node, sizeEstimate, DISTINCT | (immutable ? IMMUTABLE : 0));
        }

        private EntrySpliterator(Node<K, EntryNode<K, V>>[] array, int pos, int limit, int sizeEstimate, boolean immutable) {
            super(array, pos, limit, sizeEstimate, DISTINCT | (immutable ? IMMUTABLE : 0));
        }

        @Override
        protected NodeSpliterator<Map.Entry<K, V>, K, EntryNode<K, V>> newSubSpliterator(Node<K, EntryNode<K, V>>[] array, int pos, int limit, int sizeEstimate) {
            return new EntrySpliterator<>(array, pos, limit, sizeEstimate, hasCharacteristics(IMMUTABLE));
        }

        @Override
        protected Map.Entry<K, V> apply(EntryNode<K, V> entry) {
            return entry;
        }

    }

    static class KeySpliterator<K, V> extends NodeSpliterator<K, K, EntryNode<K, V>> {

        public KeySpliterator(Node<K, EntryNode<K, V>> node, int sizeEstimate, boolean immutable) {
            super(node, sizeEstimate, DISTINCT | (immutable ? IMMUTABLE : 0));
        }

        private KeySpliterator(Node<K, EntryNode<K, V>>[] array, int pos, int limit, int sizeEstimate, boolean immutable) {
            super(array, pos, limit, sizeEstimate, DISTINCT | (immutable ? IMMUTABLE : 0));
        }

        @Override
        protected NodeSpliterator<K, K, EntryNode<K, V>> newSubSpliterator(Node<K, EntryNode<K, V>>[] array, int pos, int limit, int sizeEstimate) {
            return new KeySpliterator<>(array, pos, limit, sizeEstimate, hasCharacteristics(IMMUTABLE));
        }

        @Override
        protected K apply(EntryNode<K, V> entry) {
            return entry.getKey();
        }

    }

    static class ValueSpliterator<K, V> extends NodeSpliterator<V, K, EntryNode<K, V>> {

        public ValueSpliterator(Node<K, EntryNode<K, V>> node, int sizeEstimate, boolean immutable) {
            super(node, sizeEstimate, (immutable ? IMMUTABLE : 0));
        }

        private ValueSpliterator(Node<K, EntryNode<K, V>>[] array, int pos, int limit, int sizeEstimate, boolean immutable) {
            super(array, pos, limit, sizeEstimate, (immutable ? IMMUTABLE : 0));
        }

        @Override
        protected NodeSpliterator<V, K, EntryNode<K, V>> newSubSpliterator(Node<K, EntryNode<K, V>>[] array, int pos, int limit, int sizeEstimate) {
            return new ValueSpliterator<>(array, pos, limit, sizeEstimate, hasCharacteristics(IMMUTABLE));
        }

        @Override
        protected V apply(EntryNode<K, V> entry) {
            return entry.getValue();
        }

    }
}