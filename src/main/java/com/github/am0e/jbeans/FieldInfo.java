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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/** Represents a field that can be accessed directly if the field is public or via an associated getter or
 * setter. 
 * The class provides a getter and a setter to set the associated field value in an object.
 * 
 * @author Anthony (ARPT)
 */
/**
 * @author anthony
 *
 */
public final class FieldInfo implements BaseInfo {
    /**
     * Field name
     */
    final String name;

    /**
     * Field name hashcode.
     */
    final int hash;

    /**
     * The bean field.
     */
    final Field field;

    /**
     * Optional setter method. If this field is public, this will contain null.
     */
    final MethodInfo setter;

    /**
     * Optional getter method. If this field is public, this will contain null.
     */
    final MethodInfo getter;

    /**
     * If the field is a parameterized List or Map, this field will contain the
     * class type of the value stored in the list or map. in the parameter. Eg:
     * List&lt;String&gt; it will contain String. For Map&lt;String,Double&gt;
     * it will contain Double.
     */
    final Class<?> actualType;

    FieldInfo(Field field, MethodInfo getter, MethodInfo setter) {

        // Get the type of the field.
        //
        this.actualType = BeanUtils.getActualTypeFromMethodOrField(null, field);
        this.field = field;
        this.setter = setter;
        this.getter = getter;
        this.name = field.getName().intern();
        this.hash = this.name.hashCode();
    }

    public Field getField() {
        return field;
    }

    public Class<?> getType() {
        return field.getType();
    }

    public Class<?> getActualType() {
        return actualType;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return field.getDeclaringClass().getName() + "#" + name;
    }

    public boolean isField() {
        return field == null ? false : true;
    }

    /**
     * Returns true if the field value can be retrieved either through the
     * public field itself or through a public getter method.
     */
    public final boolean isReadable() {
        return (Modifier.isPublic(field.getModifiers()) || getter != null);
    }

    public final boolean isSettable() {
        return (Modifier.isPublic(field.getModifiers()) || setter != null);
    }

    public final boolean isTransient() {
        return (Modifier.isTransient(field.getModifiers()));
    }

    public final Object callGetter(Object bean) throws BeanException {

        if (bean == null)
            return null;

        // If the field is public, get the value directly.
        //
        try {
            if (getter != null) {
                // Use the public getter. We will always attempt to use this
                // FIRST!!
                //
                return getter.method.invoke(bean);
            }

            if (!Modifier.isPublic(field.getModifiers())) {
                throw BeanException.fmtExcStr("Field not gettable", bean, getName(), null);
            }

            return field.get(bean);

        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw BeanException.fmtExcStr("callGetter", bean, getName(), e);

        } catch (InvocationTargetException e) {
            throw BeanUtils.wrapError(e.getCause());
        }
    }

    public final void callSetter(Object bean, Object value) throws BeanException {

        value = BeanUtils.cast(value, field.getType());

        try {
            // Use the public setter. We will always attempt to use this FIRST!!
            //
            if (setter != null) {
                setter.method.invoke(bean, value);
                return;
            }

            if (!Modifier.isPublic(field.getModifiers())) {
                throw BeanException.fmtExcStr("Field not settable", bean, getName(), null);
            }

            // If the field is public, set the value directly.
            //
            field.set(bean, value);

        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw BeanException.fmtExcStr("callSetter", bean, getName(), e);

        } catch (InvocationTargetException e) {
            throw BeanUtils.wrapError(e.getCause());
        }
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
    public <T extends Annotation> T getAnnotation(Class<T> type) {
        return field.getAnnotation(type);
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> type) {
        return field.getAnnotation(type) == null ? false : true;
    }

    @Override
    public MethodHandle getHandle(Lookup lookup, boolean setter) {
        try {
            if (setter)
                return lookup.findSetter(field.getDeclaringClass(), name, field.getType());
            else
                return lookup.findGetter(field.getDeclaringClass(), name, field.getType());

        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new BeanException(e);
        }
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
