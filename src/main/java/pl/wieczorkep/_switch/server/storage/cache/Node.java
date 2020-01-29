package pl.wieczorkep._switch.server.storage.cache;

import org.jetbrains.annotations.NotNull;

public abstract class Node<V> {
    /**
     * Time in millis of creation of this {@link LFUNode}.
     */
    private final long createdTime;
    /**
     * Value held by this {@link LFUNode}.
     */
    private V value;
    /**
     * Reference to the next @{@link LFUNode}.
     */
    private LFUNode<V> next;
    /**
     * Reference to the previous @{@link LFUNode}.
     */
    private LFUNode<V> previous;

    public Node(@NotNull V value) {
        this.value = value;
        this.createdTime = System.currentTimeMillis();
        this.next = null;
        this.previous = null;
    }

    public V getValue() {
        return value;
    }
    public void setValue(V value) {
        this.value = value;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public LFUNode<V> getNext() {
        return next;
    }
    public void setNext(LFUNode<V> next) {
        this.next = next;
    }

    public LFUNode<V> getPrevious() {
        return previous;
    }
    public void setPrevious(LFUNode<V> previous) {
        this.previous = previous;
    }
}
