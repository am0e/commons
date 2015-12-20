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

import java.util.HashMap;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * Useful wrapper class for preparing objects for views. It allows you to
 * decorate an object by adding additional properties to it so that the view can
 * access additional data. For example, given a sales order line, you can add
 * additional properties to link the line to say a shipment line. The class
 * exposes a hashmap in addition to all of the properties of it's base object.
 * Properties not in the hashmap are retrieved from the base object using
 * reflection.
 * 
 * @author ame
 */
@SuppressWarnings("serial")
public class BeanWrapper extends HashMap<String, Object> {

    /**
     * The wrapped bean object. A POJO or a Map interface.
     */
    private final Object bean;

    /**
     * The bean wrapper. Only if the bean is a POJO.
     */
    private final BeanInfo beanInfo;

    /**
     * Constructor.
     * 
     * @param bean
     *            The bean to wrap. All of it's properties are exposed as
     *            normal.
     * 
     */
    public BeanWrapper(Object bean) {
        this.bean = bean;
        this.beanInfo = BeanInfo.forClass(bean.getClass());
    }

    @Override
    public Object get(Object key) {
        Object v = super.get(key);
        if (v == null) {
            BaseInfo getter = beanInfo.getBeanGetter((CharSequence) key);

            if (getter != null) {
                v = getter.callGetter(bean);
            }
        }
        return v;
    }

    public final void set(String key, int value) {
        this.put(key, (value == 0 ? NumberUtils.INTEGER_ZERO : value));
    }

    public final void set(String key, long value) {
        this.put(key, (value == 0 ? NumberUtils.LONG_ZERO : value));
    }

    public final void set(String key, double value) {
        this.put(key, (value == 0 ? NumberUtils.DOUBLE_ZERO : value));
    }

    public final void set(String key, float value) {
        this.put(key, (value == 0 ? NumberUtils.FLOAT_ZERO : value));
    }

    public final void set(String key, boolean value) {
        this.put(key, Boolean.valueOf(value));
    }

    public final void set(String key, Object value) {
        this.put(key, value);
    }
}
