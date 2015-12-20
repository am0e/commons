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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.am0e.lib.AntLib;

/**
 * Query parser. Parses a URL query parameters and builds a map.
 * 
 * @author Anthony
 */
public class UrlQueryParser {

    protected char separator = '&';
    protected Map<String, Object> paramMap;
    protected Set<String> arrayKeys;
    private boolean multiValuesAsArray;

    public void setSeparator(char c) {
        separator = c;
    }

    public void parseMultiValuesAsArray() {
        this.multiValuesAsArray = true;
    }

    // This method is used to parse parameters encoded in the URL as follows:
    // name1=value1&name2=value2
    //
    public Map<String, Object> parse(String queryString) {

        paramMap = AntLib.newHashMap(5);
        arrayKeys = AntLib.newHashSet(5);

        if (queryString != null && queryString.isEmpty() == false) {

            // note that if a parameter with a given name already exists it will
            // be put into a list.
            //
            StringBuilder sb = new StringBuilder();
            sb.append(separator);
            sb.append(queryString);

            // make sure string ends with a trailing '&' so we get all values
            //
            if (queryString.charAt(queryString.length() - 1) != separator)
                sb.append(separator);

            char[] s = sb.toString().toCharArray();

            int posSlash = -1;
            int pathEnd = -1;
            int posEq = -1;

            // Look for &name=value& sequences.
            //
            for (int pos = 0; pos != s.length; pos++) {

                if (s[pos] == separator) {
                    if (posEq != -1) {
                        // We currently have:
                        // &name=value&
                        // | | |
                        // | | pos
                        // | posEq
                        // posSlash
                        //
                        int nameStart = posSlash + 1;
                        int nameLen = posEq - nameStart;
                        int valueLen = pos - (posEq + 1);

                        // Only if there is a name - could have "&=xyz"
                        // "&name=&" is allowed - value is an empty string.
                        //
                        if (nameLen != 0) {
                            String name = new String(s, nameStart, nameLen);
                            String value = new String(s, posEq + 1, valueLen);
                            addParam(name, value);
                        }
                        posEq = -1;
                    }
                    posSlash = pos;

                } else if (s[pos] == '=' && posEq == -1) {
                    posEq = pos;
                    if (pathEnd == -1)
                        pathEnd = posSlash;
                }
            }
        }

        if (multiValuesAsArray) {
            for (String key : arrayKeys) {
                List<?> list = (List<?>) paramMap.get(key);
                paramMap.put(key, list.toArray(new String[0]));
            }
        }

        return paramMap;
    }

    /**
     * Helper for the above method.
     */
    @SuppressWarnings("unchecked")
    protected void addParam(String key, String strValue) {

        try {
            strValue = URLDecoder.decode(strValue, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        Object value = getValue(strValue);

        if (value != null) {
            Object curValue = paramMap.get(key);

            if (curValue == null) {
                paramMap.put(key, value);

            } else {
                if (curValue instanceof List<?>) {
                    ((List<Object>) curValue).add(value);

                } else {
                    List<Object> list = new ArrayList<>(4);
                    list.add(curValue);
                    list.add(value);
                    paramMap.put(key, list);
                    arrayKeys.add(key);
                }
            }
        }
    }

    protected Object getValue(String value) {
        return value;
    }

    public String buildQueryString(String path, Map<String, Object> params) {

        StringBuilder sb = new StringBuilder();
        if (path != null) {
            sb.append(path);
            sb.append('?');
        }

        for (String name : params.keySet()) {
            Object value = params.get(name);

            // Check we have a valid value.
            //
            if (value != null) {
                if (value instanceof List) {
                    addList(sb, name, (List<?>) value);

                } else if (value.getClass().isArray()) {
                    addArray(sb, name, value);

                } else {
                    // Add name=
                    // Add the encoded value.
                    //
                    add(sb, name, value);
                }
            }
        }

        return sb.toString();
    }

    protected void addArray(StringBuilder sb, String name, Object ar) {
        int sz = Array.getLength(ar);
        for (int i = 0; i != sz; i++) {
            add(sb, name, Array.get(ar, i));
        }
    }

    protected void addList(StringBuilder sb, String name, List<?> list) {
        for (Object v : (List<?>) list) {
            if (v != null)
                add(sb, name, v);
        }
    }

    protected void add(StringBuilder sb, String name, Object val) {
        // Append '&'
        //
        if (sb.length() != 0)
            sb.append(separator);

        sb.append(name);
        sb.append('=');
        sb.append(Urls.encode(getStringValue(val)));
    }

    protected String getStringValue(Object val) {
        return val.toString();
    }
}
