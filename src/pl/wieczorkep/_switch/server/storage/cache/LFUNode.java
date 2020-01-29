package pl.wieczorkep._switch.server.storage.cache;

import org.jetbrains.annotations.NotNull;

public class LFUNode<V> extends Node<V> {
    private int usages;

    public LFUNode(@NotNull V value) {
        super(value);
        this.usages = 1;
    }

    public int getUsages() {
        return usages;
    }

    @Override
    public V getValue() {
        usages++;
        return super.getValue();
    }
}
