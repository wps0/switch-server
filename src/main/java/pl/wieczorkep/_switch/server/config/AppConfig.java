package pl.wieczorkep._switch.server.config;

import pl.wieczorkep._switch.server.storage.InMemoryStorage;
import pl.wieczorkep._switch.server.storage.Storage;
import pl.wieczorkep._switch.server.storage.cache.Cache;
import pl.wieczorkep._switch.server.storage.cache.LFUCache;
import pl.wieczorkep._switch.server.view.View;

public class AppConfig {
    private View view;
    private Storage storage;
    private Cache<String, String> configCache;

    public AppConfig(View view) {
        this.view = view;
        this.storage = new InMemoryStorage();
        this.configCache = new LFUCache<>(25);
    }


}
