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
package com.github.am0e.utils;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class CompactMap<K, V> implements Map<K, V> {

    private Object[] items;
    private int size;

    private final class CEntry implements Entry<K, V> {
        int pos;

        public CEntry(int i) {
            pos = i;
        }

        @SuppressWarnings("unchecked")
        public K getKey() {
            return (K) items[pos];
        }

        @SuppressWarnings("unchecked")
        public V getValue() {
            return (V) items[pos + 1];
        }

        @SuppressWarnings("unchecked")
        public V setValue(V value) {
            Object v = items[pos];
            items[pos + 1] = value;
            return (V) v;
        }
    }

    public CompactMap(Object... items) {
        this.items = items;
        this.size = items.length;
    }

    public CompactMap(int sz) {
        items = new Object[sz];
        size = 0;
    }

    public CompactMap() {
        items = new Object[10];
        size = 0;
    }

    public void clear() {
        size = 0;
    }

    public boolean containsKey(Object key) {
        return getPos(key) == -1 ? false : true;
    }

    public boolean containsValue(Object value) {
        for (int i = 0; i != size; i += 2)
            if (value.equals(items[i + 1]))
                return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    public V get(Object key) {
        for (int i = 0; i != size; i += 2) {
            if (key == items[i] || key.equals(items[i]))
                return (V) items[i + 1];
        }
        return null;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Set<Entry<K, V>> entrySet() {
        Set<java.util.Map.Entry<K, V>> set = new HashSet<>();
        for (int i = 0; i != size; i += 2) {
            set.add(new CEntry(i));
        }
        return set;
    }

    public Set<K> keySet() {
        AbstractSet<K> set = new AbstractSet<K>() {
            @Override
            public Iterator<K> iterator() {
                return new Iterator<K>() {
                    int pos = 0;

                    public boolean hasNext() {
                        return pos < size;
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public K next() {
                        Object o = items[pos];
                        pos += 2;
                        return (K) o;
                    }

                    @Override
                    public void remove() {
                        throw new IllegalAccessError();
                    }
                };
            }

            @Override
            public int size() {
                return size / 2;
            }
        };

        return set;
    }

    @SuppressWarnings("unchecked")
    public Collection<V> values() {
        Object[] set = new Object[size / 2];
        for (int i = 0; i != size; i += 2) {
            set[i / 2] = items[i + 1];
        }
        return Arrays.asList((V[]) set);
    }

    @SuppressWarnings("unchecked")
    public V put(K key, V value) {
        assert key != null;

        int pos = getPos(key);
        if (pos == -1) {
            ensureCapacity(size + 2);
            items[size++] = key;
            items[size++] = value;
            return value;
        } else {
            V old = (V) items[pos + 1];
            items[pos + 1] = value;
            return old;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void putAll(Map m) {
        for (Object o : m.entrySet()) {
            Map.Entry<K, V> e = (Map.Entry) o;
            put(e.getKey(), e.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        return put((K) key, null);
    }

    public int size() {
        return size / 2;
    }

    private int getPos(Object key) {
        for (int i = 0; i != size; i += 2)
            if (items[i].equals(key))
                return i;

        return -1;
    }

    void ensureCapacity(int newSize) {
        if (items == null) {
            items = new Object[newSize];
        } else {
            int oldLen = items.length;
            if (newSize > oldLen) {
                Object oldData[] = items;
                int newCapacity = (oldLen * 3) / 2 + 1;
                if (newCapacity < newSize)
                    newCapacity = newSize;
                items = new Object[newCapacity];
                System.arraycopy(oldData, 0, items, 0, size);
            }
        }
    }
}
