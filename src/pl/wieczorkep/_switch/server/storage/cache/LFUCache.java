package pl.wieczorkep._switch.server.storage.cache;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class LFUCache<K, V> implements Cache<K, V> {
    /**
     * HashMap used for O(1) get operations.
     */
    private HashMap<K, LFUNode<V>> bindings;
    private HashMap<Integer, LFUNode<V>> storage;
    /**
     * Node with the highest usage.
     */
//    private LFUNode<V> first;
    /**
     * Node with the least usage.
     */
    private LFUNode<V> last;
    /**
     * Max size of this cache.
     */
    private int maxSize;

    public LFUCache(int maxSize) {
        this.bindings = new HashMap<>();
        this.storage = new HashMap<>();
//        this.first = null;
        this.last = null;
        this.maxSize = maxSize;
    }

    @Override
    public synchronized void insert(@NotNull K key, @NotNull V value) {
        LFUNode<V> node = new LFUNode<>(value);

        if (last == null) {
//            first = node;
            storage.put(1, node);

        } else if (last.getUsages() == 1) {
            last.setPrevious(node);
            node.setNext(last);
//            node.setPrevious(); nie potrzebne ustawianie ostatniej node referencji do poczatku

        } else {
            storage.putIfAbsent(1, node);
        }

        last = node;
        bindings.put(key, node);
    }

    @Override
    public synchronized V lookup(@NotNull K key) {
        LFUNode<V> node = bindings.get(key);

        if (node != null) {
            int newUsages = node.getUsages() + 1;
            LFUNode<V> headNode = storage.get(newUsages);

            if (headNode == null) {
                /*
                * if headNode does not exists, thereby the LinkedList for specified usage amount does not exists,
                *   so create new LinkedList by setting the node as its head.
                */
                storage.put(newUsages, node);
            } else {
                // headNode is first, thus it holds reference to the last node.
                node.setNext(headNode.getNext());
                headNode.setNext(node);
            }

            return node.getValue();
        }

        return null;
    }

    @Override
    public synchronized void flush() {
        bindings.clear();
        storage.clear();
//        first = null;
        last = null;
    }

//    private void cleanupLast
}
