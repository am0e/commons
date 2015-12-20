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

/**
 * Represents a field that can be accessed directly if the field is public or
 * via an associated getter or setter. The class provides a getter and a setter
 * to set the associated field value in an object.
 * 
 * @author Anthony (ARPT)
 */
public final class PropertyInfo implements BaseInfo {
    /**
     * Property name
     */
    String name;

    /**
     * Property name hashcode.
     */
    int hash;

    /**
     * Getter. This may be a getter method or a public field. Note for public
     * fields, getter and setter point to the same {@link FieldInfo} object.
     */
    BaseInfo getter;

    /**
     * Setter. This may be a setter method or a public field.
     */
    BaseInfo setter;

    /**
     * If the field is a parameterized List or Map, this field will contain the
     * class type of the value stored in the list or map. in the parameter. Eg:
     * List&lt;String&gt; it will contain String. For Map&lt;String,Double&gt;
     * it will contain Double.
     */
    Class<?> actualType;

    /**
     * Field type. Eg List&lt;Double&gt;
     */
    Class<?> fieldType;

    PropertyInfo(BaseInfo getter, BaseInfo setter, Class<?> fieldType, Class<?> actualType) {
        this.getter = getter;
        this.setter = setter;
        this.fieldType = fieldType;
        this.actualType = actualType;
        this.name = getter.getName();
        this.hash = name.hashCode();
    }

    public Class<?> getType() {
        return fieldType;
    }

    public Class<?> getActualType() {
        return actualType;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return getter.getName() + "#" + name;
    }

    public final Object callGetter(Object bean) throws BeanException {
        // Forward to the getter.
        //
        return getter.callGetter(bean);
    }

    public final void callSetter(Object bean, Object value) throws BeanException {
        // Forward to the setter.
        //
        setter.callSetter(bean, value);
    }

    /**
     * Converts a value into a value of the bean type.
     * 
     * @param value
     *            The value to convert.
     * @return If the value could not be converted, the value itself is
     *         returned. For example: if (beanField.valueOf(strVal)==strVal)
     *         throw new IllegalArgumentException();
     */
    public final Object valueOf(Object value) {
        return BeanUtils.cast(value, actualType);
    }

    @Override
    public MethodHandle getHandle(Lookup lookup, boolean setter) {
        throw new IllegalAccessError();
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> a) {
        return getter.isAnnotationPresent(a);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> type) {
        return getter.getAnnotation(type);
    }

    public BaseInfo setter() {
        return setter;
    }

    @Override
    public String makeSignature(StringBuilder sb) {
        sb.setLength(0);
        sb.append(getType().toString());
        sb.append(' ');
        sb.append(getName());
        return sb.toString();
    }
}
