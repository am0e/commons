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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.github.am0e.cache.ICache;
import com.github.am0e.cache.impl.SimpleMemCache;

public class DefaultAntLibProvider implements AntLibProvider {

    public String formatMessage(String code, Locale locale, Object... args) {
        return null;
    }

    @Override
    public <K, V> Map<K, V> newHashMap() {
        return new HashMap<>();
    }

    @Override
    public <K, V> Map<K, V> newHashMap(int size) {
        return new HashMap<>(size);
    }

    @Override
    public <V> Set<V> newHashSet() {
        return new HashSet<>();
    }

    @Override
    public <V> Set<V> newHashSet(int size) {
        return new HashSet<>(size);
    }

    @Override
    public <T> ICache<T> getCache(String id) {
        return SimpleMemCache.getCache(id);
    }

    @Override
    public <V> List<V> newList() {
        return new ArrayList<V>();
    }

    @Override
    public <V> List<V> newList(int size) {
        return new ArrayList<V>(size);
    }
}
