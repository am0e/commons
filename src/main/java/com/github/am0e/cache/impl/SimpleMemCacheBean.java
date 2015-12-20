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

import com.github.am0e.utils.FrequencyInterval;

/**
 * Cache bean for wiring up a cache in the beans configuration file
 */
public class SimpleMemCacheBean {
    private String cacheName;
    private boolean useSoftReferences = true;
    private int concurrencyLevel = 10;
    private String ttl = "never";

    public SimpleMemCacheBean() {
    }

    public SimpleMemCacheBean(String name) {
        this.cacheName = name;
    }

    public final boolean isUseSoftReferences() {
        return useSoftReferences;
    }

    public final void setUseSoftReferences(boolean useSoftReferences) {
        this.useSoftReferences = useSoftReferences;
    }

    public final String getCacheName() {
        return cacheName;
    }

    public final void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public final void setTtl(String expr) {
        ttl = expr;
    }

    public void setConcurrencyLevel(int concurrencyLevel) {
        this.concurrencyLevel = concurrencyLevel;
    }

    public final int getConcurrencyLevel() {
        return concurrencyLevel;
    }

    public long getTtlVal() {
        int ttl;
        if (this.ttl.equals("never")) {
            ttl = -1;
        } else {
            ttl = (int) FrequencyInterval.parse(this.ttl).intervalAsMilliseconds();
        }
        return ttl;
    }
}
