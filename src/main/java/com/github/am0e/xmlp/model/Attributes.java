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
package com.github.am0e.xmlp.model;

public final class Attributes {
    private Attribute[] al;
    private int size;
    public final static Attributes EMPTY_LIST = new Attributes();
    public final static Attribute[] EMPTY_ARRAY = new Attribute[0];

    public static Attributes create(Attribute[] list) {
        if (list.length == 0)
            return EMPTY_LIST;
        else
            return new Attributes(list);
    }

    private Attributes() {
        al = EMPTY_ARRAY;
        size = 0;
    }

    private Attributes(Attribute[] list) {
        set(list);
    }

    public void set(Attribute[] list) {
        al = list;
        size = al.length;
    }

    public Attribute get(CharSequence name) {
        for (int i = 0; i != size; i++) {
            if (al[i].getQName().equals(name))
                return al[i];
        }

        return null;
    }

    public Attribute get(CharSequence nsUri, CharSequence name) {
        for (int i = 0; i != size; i++) {
            if (al[i].getQName().equals(name) && nsUri.equals(al[i].getUri()))
                return al[i];
        }

        return null;
    }

    public Object getValue(CharSequence name) {
        Attribute a = get(name);
        return a == null ? null : a.getValue();
    }

    public Attribute[] toArray() {
        return al;
    }

    public int size() {
        return size;
    }

    public Attribute get(int index) {
        return al[index];
    }

    /**
     * Retrieves an attribute value by name.
     * 
     * @param name
     *            The name of the attribute to retrieve.
     * @return The <code>Attr</code> value as a string, or the empty string if
     *         that attribute does not have a specified or default value.
     */
    public String getStringValue(CharSequence name) {
        Attribute a = get(name);
        return a == null ? "" : a.getValue().toString();
    }

    public boolean hasAttribute(String name) {
        Attribute a = get(name);
        return a == null ? false : true;
    }
}
