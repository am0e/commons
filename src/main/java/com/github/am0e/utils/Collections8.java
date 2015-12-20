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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.github.am0e.lib.AntLib;

public final class Collections8 {

    /**
     * Create a map from name value pair
     */
    public static Map<String, Object> asMap(String n1, Object v1) {
        Map<String, Object> m = AntLib.newHashMap(1);
        m.put(n1, v1);
        return m;
    }

    /**
     * Create a map from name value pairs
     */
    public static Map<String, Object> asMap(String n1, Object v1, String n2, Object v2) {
        Map<String, Object> m = AntLib.newHashMap(2);
        m.put(n1, v1);
        m.put(n2, v2);
        return m;
    }

    /**
     * Create a map from name value pairs
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> asMap(String n1, Object v1, String n2, Object v2, String n3, Object v3) {
        Map<K, V> m = AntLib.newHashMap(3);
        m.put((K) n1, (V) v1);
        m.put((K) n2, (V) v2);
        m.put((K) n3, (V) v3);
        return m;
    }

    /**
     * Create a map from name value pairs
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> asMap(Object... nameValues) {

        if (nameValues == null) {
            return null;
        }

        if (nameValues.length % 2 == 1) {
            throw new IllegalArgumentException("nameValues is not an even sized array.");
        }

        Map<K, V> fields = AntLib.newHashMap(nameValues.length);
        for (int i = 0; i != nameValues.length; i += 2) {
            fields.put((K) nameValues[i], (V) nameValues[i + 1]);
        }

        return fields;
    }

    /**
     * Returns the array as a list. Unlike {@link Arrays#asList(Object...)} the
     * list allows further add operations.
     *
     * @param array
     *            to add to the list.
     * @return an ArrayList object containing a copy of the list parameter.
     */
    @SafeVarargs
    public static <T> List<T> asList(T... ar) {
        List<T> list = AntLib.newList(ar.length);

        for (T it : ar) {
            list.add(it);
        }

        return list;
    }
}
