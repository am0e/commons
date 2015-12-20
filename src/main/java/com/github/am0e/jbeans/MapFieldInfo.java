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

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;

public final class MapFieldInfo implements BaseInfo {
    private String key;

    public MapFieldInfo(String key) {
        this.key = key;
    }

    @SuppressWarnings("unchecked")
    public void callSetter(Object bean, Object value) throws BeanException {
        ((Map<Object, Object>) bean).put(key, value);
    }

    @SuppressWarnings("unchecked")
    public Object callGetter(Object bean) {
        return ((Map<Object, Object>) bean).get(key);
    }

    public Class<?> getType() {
        return Object.class;
    }

    public Class<?> getActualType() {
        return Object.class;
    }

    public String getName() {
        return key;
    }

    @Override
    public MethodHandle getHandle(Lookup lookup, boolean setter) {
        throw new IllegalAccessError();
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> a) {
        return false;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> type) {
        return null;
    }

    @Override
    public String makeSignature(StringBuilder sb) {
        sb.setLength(0);
        sb.append(getName());
        return sb.toString();
    }
}
