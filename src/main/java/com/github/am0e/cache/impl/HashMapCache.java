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

import java.util.Map;

import com.github.am0e.cache.ICache;

public class HashMapCache<T> implements ICache<T> {

    private Map<Object, T> map;

    public HashMapCache(Map<Object, T> map) {
        this.map = map;
    }

    @Override
    public void put(Object key, T value) {
        this.map.put(key, value);
    }

    @Override
    public void put(Object key, T value, long ttl) {
        this.map.put(key, value);
    }

    @Override
    public T get(Object key) {
        return map.get(key);
    }

    @Override
    public void remove(Object key) {
        map.remove(key);
    }

    @Override
    public void clearCache() {
        map.clear();
    }
}
