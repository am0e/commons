/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.github.am0e.cache.impl;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.am0e.cache.ICache;
import com.github.am0e.lib.AntLib;
import com.github.am0e.msgs.Msgs;

/**
 * Simple in memory cache. Uses {@link SoftReference} for items in the cached so
 * that the GC can reclaim memory.
 * 
 * @author Anthony (ARPT)
 *
 */
public class SimpleMemCache<T> implements ICache<T>, SimpleMemCacheMBean {

    private static final Logger log = LoggerFactory.getLogger(SimpleMemCache.class);

    private long ttl;
    private String cacheName;
    private boolean softRefs;
    private volatile long countAdded;
    private volatile int countHits;
    private volatile int countMisses;
    private volatile int countExpires;
    private volatile int countEvictions;
    private volatile long cleanCache;
    private final ReentrantLock cleanLock;
    private Map<Object, CacheItem> cache;
    private final static int CLEAN_CACHE_COUNT = 1000;

    private static HashMap<String, ICache<?>> globalCaches = new HashMap<>();

    public static <T> ICache<T> getCache(Class<?> claz) {
        String name = claz.getName();
        return getCache(name);
    }

    public static <T> ICache<T> getCache(Class<?> claz, String name) {
        name = claz.getName() + "$" + name;
        return getCache(name);
    }

    @SuppressWarnings("unchecked")
    public static <T> ICache<T> getCache(String id) {
        synchronized (globalCaches) {
            ICache<T> cache = (ICache<T>) globalCaches.get(id);

            if (cache == null) {
                cache = registerCache(new SimpleMemCacheBean(id));
            }

            return cache;
        }
    }

    static public <T> ICache<T> registerCache(SimpleMemCacheBean def) {
        synchronized (globalCaches) {
            SimpleMemCache<T> cache = new SimpleMemCache<>(def);
            globalCaches.put(cache.cacheName, cache);
            return cache;
        }
    }

    public static ICache<?>[] getCaches() {
        synchronized (globalCaches) {
            return globalCaches.values().toArray(new SimpleMemCache[0]);
        }
    }

    SimpleMemCache(SimpleMemCacheBean def) {
        this.ttl = def.getTtlVal();
        this.cacheName = def.getCacheName();
        this.cache = new ConcurrentHashMap<>(16, 0.75F, 8);
        this.softRefs = def.isUseSoftReferences();
        this.cleanLock = new ReentrantLock();
        this.cleanCache = CLEAN_CACHE_COUNT;

        if (def.getConcurrencyLevel() > 0) {
            this.cache = new ConcurrentHashMap<>(16, 0.75F, def.getConcurrencyLevel());
        } else {
            Map<Object, CacheItem> map = AntLib.newHashMap();
            this.cache = Collections.synchronizedMap(map);
        }

        log.debug(Msgs.format("Cache name={} TTL={} CL={}", cacheName, ttl, def.getConcurrencyLevel()));
    }

    public void put(Object key, T value) {
        // this.cache.values().toArray(new Object[0]);
        // this.cache.keySet().toArray(new Object[0]);
        if (ttl <= 0)
            put(key, value, -1);
        else
            put(key, value, ttl);
    }

    public void put(Object key, T value, long ttl) {
        long expiryTime = (ttl == -1 ? -1 : (System.currentTimeMillis() + ttl));

        if (softRefs) {
            cache.put(key, new SoftRefCacheItem(value, expiryTime));
        } else {
            cache.put(key, new ObjCacheItem(value, expiryTime));
        }

        countAdded += 1;

        if (countAdded >= cleanCache) {
            clean();
        }
    }

    public T get(Object key) {
        CacheItem item = cache.get(key);

        if (item == null) {
            // Never cached.
            //
            return null;

        } else {
            // Get the object, it may have been GC'd if using a reference.
            //
            @SuppressWarnings("unchecked")
            T it = (T) item.get();

            // has the object been GC?
            //
            if (it == null) {
                log.debug("Evicted {}", key);
                remove(key); // Remove the entry and key from cache.
                countEvictions++;
                countMisses++;
                return null;
            }

            // Has the item expired?
            //
            if (item.checkExpired()) {
                log.debug("Expired {}", key);
                remove(key); // Remove the entry and key from the cache.
                countExpires++;
                countMisses++;
                return null;
            }

            // Hit.
            //
            countHits++;
            return it;
        }
    }

    public void remove(Object key) {
        cache.remove(key);
    }

    public void clearCache() {
        this.countAdded = 0;
        this.countHits = 0;
        this.countMisses = 0;
        this.countExpires = 0;
        this.countEvictions = 0;
        cache.clear();
    }

    public final Collection<CacheItem> getCachedObjects() {
        return cache.values();
    }

    public final int getSize() {
        return cache.size();
    }

    public final String getCacheName() {
        return cacheName;
    }

    public final long getCountAdded() {
        return countAdded;
    }

    public final long getCountHits() {
        return countHits;
    }

    public final long getCountMisses() {
        return countMisses;
    }

    public final long getTtl() {
        return ttl;
    }

    public final boolean isSoftRefs() {
        return softRefs;
    }

    public final int getCountExpires() {
        return countExpires;
    }

    public final int getCountEvictions() {
        return countEvictions;
    }

    /**
     * Clean the cache removing entries evicted from memory.
     */
    private void clean() {
        if (cleanLock.tryLock()) {
            try {
                Iterator<Entry<Object, CacheItem>> iterator = cache.entrySet().iterator();

                while (iterator.hasNext()) {
                    CacheItem e = iterator.next().getValue();
                    if (e != null) {
                        if (e.get() == null) {
                            iterator.remove(); // Remove the entry and key from
                                               // the cache.
                            countEvictions++;
                        } else if (e.checkExpired()) {
                            iterator.remove(); // Remove the entry and key from
                                               // the cache.
                            countExpires++;
                            countEvictions++;
                        }
                    }
                }

                cleanCache = (countAdded + CLEAN_CACHE_COUNT);

            } finally {
                cleanLock.unlock();
            }
        }
    }
}
