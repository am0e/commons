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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.am0e.cache.ICache;

public interface AntLibProvider {
    /**
     * /** Creates a new Java Hash Map.
     * 
     * @return
     */
    public <K, V> Map<K, V> newHashMap();

    /**
     * Creates a new Java Hash Map using the default size specified.
     * 
     * @return
     */
    public <K, V> Map<K, V> newHashMap(int size);

    /**
     * Creates a new Java Hash Set.
     * 
     * @return
     */
    public <V> Set<V> newHashSet();

    /**
     * Creates a new Java Hash Set using the default size specified.
     * 
     * @return
     */
    public <V> Set<V> newHashSet(int size);

    /**
     * Get a cache object for the specified id.
     * 
     * @param id
     * @return
     */
    public <T> ICache<T> getCache(String id);

    public <V> List<V> newList();

    public <V> List<V> newList(int size);
}
