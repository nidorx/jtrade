package com.github.nidorx.jtrade.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
public class SimpleCache<T> {

    private final Map<String, Entry<T>> CACHE = new LinkedHashMap(21, .75F, true) {
        @Override
        public boolean removeEldestEntry(Map.Entry eldest) {
            return size() > 20;
        }
    };

    public T get(String key, Supplier exec, long ttl) {
        T out;
        if (!CACHE.containsKey(key)) {
            out = (T) exec.get();
            CACHE.put(key, new Entry(out, ttl));
        } else {
            Entry<T> entry = CACHE.get(key);
            if (entry.isExpired()) {
                CACHE.remove(key);
                out = (T) exec.get();
                CACHE.put(key, new Entry(out, ttl));
            } else {
                out = entry.getValue();
            }
        }
        return out;
    }

    public void remove(String key) {
        CACHE.remove(key);
    }

    private static class Entry<T> {

        private final T value;
        private final long expiration;

        public Entry(T value, long expiration) {
            this.value = value;
            this.expiration = System.currentTimeMillis() + expiration;
        }

        public boolean isExpired() {
            return expiration < System.currentTimeMillis();
        }

        public T getValue() {
            return this.value;
        }
    }

}
