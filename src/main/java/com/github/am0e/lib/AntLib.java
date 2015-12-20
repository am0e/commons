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
package com.github.am0e.lib;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.am0e.cache.ICache;

public final class AntLib {
    private static AntLibProvider ext = new DefaultAntLibProvider();

    public static void setAntLibProvider(AntLibProvider ext) {
        AntLib.ext = ext;
    }

    public static <K, V> Map<K, V> newHashMap(int size) {
        return ext.newHashMap(size);
    }

    public static <K, V> Map<K, V> newHashMap() {
        return ext.newHashMap();
    }

    public static <K, V> Map<K, V> newHashMap(Map<K, V> map) {
        Map<K, V> m = ext.newHashMap();
        m.putAll(map);
        return m;
    }

    public static <V> Set<V> newHashSet() {
        return ext.newHashSet();
    }

    public static <V> Set<V> newHashSet(int size) {
        return ext.newHashSet(size);
    }

    public static <V> Set<V> newHashSet(Collection<V> c) {
        Set<V> m = ext.newHashSet(c.size());
        m.addAll(c);
        return m;
    }

    public static <V> List<V> newList() {
        return ext.newList();
    }

    public static <V> List<V> newList(int size) {
        return ext.newList(size);
    }

    public static <V> List<V> newList(Collection<V> c) {
        List<V> m = ext.newList(c.size());
        m.addAll(c);
        return m;
    }

    public static <T> ICache<T> getCache(Class<?> claz) {
        return ext.getCache(claz.getName());
    }

    public static <T> ICache<T> getCache(Class<?> claz, String id) {
        id = claz.getName().concat("$").concat(id);
        return ext.getCache(id);
    }

    public static <T> ICache<T> getCache(String name) {
        return ext.getCache(name);
    }

    public static boolean isNotObjectEmpty(Object value) {
        return isObjectEmpty(value) == false;
    }

    public static boolean isObjectEmpty(Object value) {
        if (value == null)
            return true;

        if (value instanceof String) {
            if (((String) value).length() == 0) {
                return true;
            }

        } else if (value instanceof Collection) {
            if (((Collection<?>) value).size() == 0) {
                return true;
            }

        } else if (value instanceof Map) {
            if (((Map<?, ?>) value).size() == 0) {
                return true;
            }

        } else if (value.getClass().isArray()) {
            if (Array.getLength(value) == 0) {
                return true;
            }
        }

        return false;
    }
}
