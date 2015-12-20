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
package com.github.am0e.jbeans;

import com.github.am0e.cache.ICache;
import com.github.am0e.lib.AntLib;

/**
 * ClassCache. Classes are cached by the name and classloader in a cache.
 * {@link ClassLoader#loadClass(String)} is somewhat slow so we hide it behind a
 * class cache. In a loop of 10000000 iterations we can reduce the call time
 * from 7245ms to 1811ms. A reduction of 75%.
 * 
 * @author anthony
 *
 */
public final class ClassCache {

    /**
     * The Cache key is the class name plus the class loader.
     * 
     * @author anthony
     *
     */
    private static final class CacheKey {
        ClassLoader cl;
        String name;

        CacheKey(ClassLoader cl, String name) {
            this.cl = cl;
            this.name = name;
        }

        @Override
        public int hashCode() {
            return cl.hashCode() ^ name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            final CacheKey other = (CacheKey) obj;
            return cl == other.cl && name.equals(other.name);
        }
    };

    /**
     * Class cache.
     */
    private static ICache<Class<?>> cache = AntLib.getCache(ClassCache.class);

    public static Class<?> loadClass(ClassLoader cl, String className) throws ClassNotFoundException {
        // Construct the key.
        //
        CacheKey it = new CacheKey(cl, className);

        Class<?> claz = cache.get(it);
        if (claz == null) {
            claz = cl.loadClass(className);
            cache.put(it, claz);
        }
        return claz;
    }
}
