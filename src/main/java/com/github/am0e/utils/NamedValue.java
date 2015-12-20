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

/**
 * A general purpose Named String variant. Useful for creating property sets
 * containing names string values.
 * 
 * @see StringVariant
 * @author Anthony (ARPT)
 */
public final class NamedValue implements Comparable<NamedValue> {

    /**
     * The name
     */
    public String name;
    public Object value;

    public NamedValue() {
    }

    public NamedValue(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int compareTo(NamedValue o) {
        return name.compareToIgnoreCase(o.name);
    }

    public Object getValue() {
        return value;
    }

    public String toString() {
        return name + "=" + value;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final void setValue(Object value) {
        this.value = value;
    }
}
