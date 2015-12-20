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

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;

import com.github.am0e.lib.AntLib;
import com.github.am0e.msgs.Msgs;

public class MultiValueMap implements Map<String, Object> {

    private final Map<String, Object> map;

    @SuppressWarnings("unchecked")
    public static final MultiValueMap EMPTY_MAP = new MultiValueMap(Collections.EMPTY_MAP);

    public MultiValueMap() {
        map = AntLib.newHashMap();
    }

    @SuppressWarnings("unchecked")
    public MultiValueMap(Map<String, ?> args) {
        map = (Map<String, Object>) args;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int size() {
        return map.size();
    }

    public final void set(String name, Object value) {
        map.put(name, value);
    }

    public final Set<String> keySet() {
        return map.keySet();
    }

    public final boolean contains(String name) {
        return map.containsKey(name);
    }

    public final boolean getBoolean(String name) {
        Object o = getObject(name);

        if (o instanceof Boolean) {
            return (Boolean) o;
        } else {
            if ("1".equals(o)) {
                return true;
            } else {
                return BooleanUtils.toBoolean((String) o);
            }
        }
    }

    public final boolean getBoolean(String name, boolean defaultV) {
        Object o = map.get(name);
        if (o == null)
            return defaultV;
        else
            return getBoolean(name);
    }

    public final int getInteger(String name) {
        Object o = getObject(name);
        return (o instanceof Number ? ((Number) o).intValue() : Integer.parseInt((String) o));
    }

    public final int getInteger(String name, int defaultV) {
        Object o = map.get(name);

        if (o == null || "".equals(o))
            return defaultV;
        else
            return getInteger(name);
    }

    public final long getLong(String name) {
        Object o = getObject(name);
        return (o instanceof Long ? ((Long) o) : Long.parseLong((String) o));
    }

    public final long getLong(String name, long defaultV) {
        Object o = map.get(name);

        if (o == null || "".equals(o))
            return defaultV;
        else
            return getLong(name);
    }

    public final double getDouble(String name) {
        Object o = getObject(name);
        return o instanceof Double ? ((Double) o) : Double.parseDouble((String) o);
    }

    public final double getDouble(String name, double defaultV) {
        Object o = map.get(name);

        if (o == null || "".equals(o))
            return defaultV;
        else
            return getDouble(name);
    }

    /**
     * Get a number. This method uses DecimalFormat.parse() to parse from a
     * string to a number. This allows for commas, etc in the input string.
     * 
     * @param name
     * @return the value as a Number.
     */
    public final Number getNumber(String name) {
        Object o = getObject(name);
        return o instanceof Number ? (Number) o : parseAsNumber(o);
    }

    private Number parseAsNumber(Object o) {
        try {
            return DecimalFormat.getInstance().parse((String) o);

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public final Number getNumber(String name, int defaultV) {
        Object o = getObject(name);
        if (o == null || "".equals(o))
            return Integer.valueOf(defaultV);
        else
            return getNumber(name);
    }

    public final String getString(String name) {
        String s = (String) getObject(name);
        if (s.isEmpty()) {
            throw new RuntimeException(Msgs.format("Parameter is missing: {}", name));
        }
        return s;
    }

    public Date getDate(String name) {
        return new Date(getLong(name));
    }

    public Date getDate(String name, Date defaultV) {
        long time = getLong(name, 0);
        return time == 0 ? defaultV : new Date(time);
    }

    public final String getString(String name, String defaultV) {
        String s = (String) map.get(name);

        if (s == null || s.isEmpty())
            return defaultV;
        else
            return s;
    }

    public final Object getObject(String name) {
        Object o = map.get(name);

        if (o == null)
            throw new RuntimeException(Msgs.format("Value for param: {}", name));
        else
            return o;
    }

    public final Object getObject(String name, Object defaultV) {
        Object o = map.get(name);
        return (o == null ? defaultV : o);
    }

    public final String[] getKeys(String name) {

        List<String> keys = AntLib.newList();

        for (String key : map.keySet()) {
            if (key.startsWith(name)) {
                keys.add(key);
            }
        }

        return sortKeys(keys);
    }

    public final List<MultiValueMap> getSubValues(String prefix) {

        // Get keys sorted:
        // ruledef_params[1].typeId
        // ruledef_params[1].op
        // ruledef_params[2].typeId
        // ruledef_params[2].op
        //
        String[] keys = getKeys(prefix);
        List<MultiValueMap> list = AntLib.newList();
        MultiValueMap map = null;
        String curNdx = null;

        for (String key : keys) {

            // ruledef_params[1].typeId
            int dot = key.indexOf('.');

            // ruledef_params[1]
            String ndx = key.substring(0, dot);

            // typeId
            String fld = key.substring(dot + 1);

            if (ndx.equals(curNdx) == false) {
                map = new MultiValueMap();
                list.add(map);
                curNdx = ndx;
            }

            // set the subkey value.
            map.set(fld, this.map.get(key));
        }

        return list;
    }

    private String[] sortKeys(List<String> keys) {
        // Sort by order, eg:
        // name.0
        // name.1
        // name.2
        // etc.
        //
        String[] ar = keys.toArray(new String[0]);
        Arrays.sort(ar);
        return ar;
    }

    public final boolean hasValue(String name, String value) {
        return value.equals(map.get(name));
    }

    public final boolean hasValue(String name) {
        Object v = map.get(name);

        if (v == null)
            return false;

        if (v instanceof String && ((String) v).isEmpty())
            return false;

        return true;
    }

    public String[] getStringArray(String name) {
        Object o = getObject(name);
        if (o instanceof Object[]) {
            return (String[]) o;
        } else {
            return new String[] { o.toString() };
        }
    }

    public long[] getLongArray(String name) {
        Object o = getObject(name);
        if (o instanceof Object[]) {
            String[] sa = (String[]) o;
            long[] la = new long[sa.length];
            for (int i = 0; i != sa.length; i++) {
                la[i] = Long.parseLong(sa[i]);
            }
            return la;
        } else {
            return new long[] { Long.parseLong(o.toString()) };
        }
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return map.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        return map.entrySet();
    }
}
