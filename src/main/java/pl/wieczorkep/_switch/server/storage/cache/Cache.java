package pl.wieczorkep._switch.server.storage.cache;

import org.jetbrains.annotations.NotNull;

/**
 * Classes offering portable cache should implement this {@link Cache} interface.
 *
 * @param <K> Type of keys to be used.
 * @param <V> Type of objects to be cached.
 */
public interface Cache<K, V> {
    /**
     * Adds specified value to be accessible (via {@link #lookup(Object)} method) using given key.
     *
     * @param key Key under which given value will be available
     * @param value Value
     *
     * @see #lookup(Object)
     */
    void insert(@NotNull K key, @NotNull V value);

    /**
     * Gets the value sitting under given key.
     *
     * @param key Key under which value should be searched.
     * @return Value in the cache available under specified key or null, if value not found.
     *
     * @see #insert(Object, Object)
     */
    V lookup(@NotNull K key);

    /**
     * Removes {@link Node} hidden under specified key and returns the value.
     *
     * @param key Key which will be removed.
     * @return Value that was available under specified key or null, if key incorrect.
     *
     * @throws UnsupportedOperationException if operation is not supported in the implementation.
     */
    default V delete(@NotNull K key) {
        throw new UnsupportedOperationException("operation is not supported in the current implementation");
    }

    /**
     * Used to return queued value and remove it from internal storage, thereby allowing other
     * {@link Node Nodes} to be stored in this {@link Cache}.
     *
     * @param key Key under which value will be searched.
     * @return Value stored under specified key or null, if key does not existed.
     *
     * @throws UnsupportedOperationException if operation is not supported in the implementation.
     *
     * @see Cache
     */
    default V poll(@NotNull K key) {
        throw new UnsupportedOperationException("operation is not supported in the current implementation");
    }

    /**
     * Clears this cache.
     *
     * @throws UnsupportedOperationException if operation is not supported in the implementation.
     */
    void flush();
}
