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

import java.util.Map;

/**
 * Wraps a bean and provides a Map like interface to the fields of a POJO or a
 * Map interface.
 * 
 * @author Anthony (ARPT)
 */
public class BeanAccessor {

    /**
     * The wrapped bean object. A POJO or a Map interface.
     */
    private Object bean;

    /**
     * The bean wrapper. Only if the bean is a POJO.
     */
    private BeanClassWrapper wrapper;

    public BeanAccessor(Object bean) {
        if (bean != null) {
            setBean(bean);
        }
    }

    @SuppressWarnings("unchecked")
    public Object get(String fieldName) {
        if (wrapper == null)
            return ((Map<String, Object>) bean).get(fieldName);
        else
            return wrapper.callGetter(bean, fieldName);
    }

    @SuppressWarnings("unchecked")
    public void set(String fieldName, Object value) {
        if (wrapper == null) {
            ((Map<String, Object>) bean).put(fieldName, value);
        } else {
            wrapper.callSetter(bean, fieldName, value);
        }
    }

    /**
     * Allows for utilising a single {@link BeanAccessor} to process many beans
     * of the same class.
     */
    public final void setBean(Object bean) {
        if (bean instanceof Map) {
            wrapper = null;
        } else {
            if (wrapper == null || wrapper.getTheClass() != bean.getClass()) {
                wrapper = BeanClassWrapper.getBeanClassWrapper(bean.getClass());
            }
        }
        this.bean = bean;
    }

    /**
     * Returns the bean object.
     */
    public final Object getBean() {
        return bean;
    }

    public final BeanClassWrapper getWrapper() {
        return wrapper;
    }

    public void newInstance() {
        bean = wrapper.newInstance();
    }
}
