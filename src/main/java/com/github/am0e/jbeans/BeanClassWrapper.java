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

import java.util.IdentityHashMap;

public final class BeanClassWrapper {

    private BeanInfo beanInfo;

    /**
     * Local cache of bean info objects
     */
    private IdentityHashMap<Class<?>, BeanInfo> cache;

    /**
     * Gets the BeanClassWrapper associated with the specified class.
     * 
     * @param clss
     * @return A ClassDef.
     */
    public static BeanClassWrapper getBeanClassWrapper(Class<?> clss) {
        return new BeanClassWrapper(clss);
    }

    BeanClassWrapper(BeanInfo beanInfo) {
        this.beanInfo = beanInfo;
    }

    /**
     * Constructs the ClassDef from the specified class. Also gets the ClassDef
     * for the superclass.
     * 
     * @param beanClass
     *            The java Class.
     */
    public BeanClassWrapper(Class<?> beanClass) {
        this.beanInfo = BeanInfo.forClass(beanClass);
    }

    public BeanClassWrapper() {
    }

    public BeanInfo getBeanInfo() {
        return beanInfo;
    }

    /**
     * Gets the real Java Class.
     * 
     * @return
     */
    public final Class<?> getTheClass() {
        return beanInfo.getBeanClass();
    }

    public final boolean setToSuper() {
        BeanInfo sup = beanInfo.getSuperBeanInfo();
        if (sup == null)
            return false;
        this.beanInfo = sup;
        return true;
    }

    public final void setClass(Class<?> beanClass) {
        if (beanInfo != null && beanInfo.getBeanClass() == beanClass)
            return;

        // Do we have an existing?
        //
        if (this.beanInfo != null) {

            // Cache existing.
            //
            if (cache == null) {
                cache = new IdentityHashMap<>();
            }

            cache.put(beanInfo.getBeanClass(), beanInfo);

            // Try local cache first.
            //
            beanInfo = cache.get(beanClass);

            if (beanInfo != null) {
                return;
            }
        }

        beanInfo = BeanInfo.forClass(beanClass);
    }

    public final BeanClassWrapper clone() {
        return new BeanClassWrapper(beanInfo);
    }

    /**
     * Returns an accessible field of the class.
     * 
     * @param name
     *            The name of the field to get.
     * @return
     */
    public final FieldInfo getField(CharSequence name) {
        return beanInfo.getPublicField(name);
    }

    public final FieldInfo[] getPublicFields() {
        return beanInfo.getDeclaredPublicFields();
    }

    public final PropertyInfo getProperty(CharSequence name) {
        return beanInfo.getProperty(name);
    }

    public final PropertyInfo[] getDeclaredProperties() {
        return beanInfo.getDeclaredProperties();
    }

    public final MethodInfo[] getDeclaredPublicMethods() {
        return beanInfo.getDeclaredPublicMethods();
    }

    public final MethodInfo getPublicMethod(CharSequence name, int nargs) {
        return beanInfo.getPublicMethod(name, nargs);
    }

    /**
     * Creates an instance of the bean.
     * 
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    public final <T> T newInstance() throws BeanException {
        return (T) BeanUtils.newInstance(beanInfo.getBeanClass());
    }

    public final Object callGetter(Object bean, CharSequence name) throws BeanException {

        BaseInfo getter = beanInfo.getBeanGetter(name);

        if (getter == null) {
            throw BeanException.fmtExcStr("callGetter", bean, name, null);
        }

        return getter.callGetter(bean);
    }

    public final Object tryCallGetter(Object bean, CharSequence name) throws BeanException {
        BaseInfo getter = beanInfo.getBeanGetter(name);

        return getter == null ? null : getter.callGetter(bean);
    }

    public final boolean callSetter(Object bean, CharSequence name, Object value) throws BeanException {
        // Map the name into a getter method.
        //
        BaseInfo setter = beanInfo.getBeanSetter(name, value == null ? null : value.getClass());

        if (setter != null) {
            setter.callSetter(bean, value);
            return true;
        } else {
            return false;
        }
    }

    public final BaseInfo getBeanSetter(CharSequence fieldName, Class<?> type) throws BeanException {
        return beanInfo.getBeanSetter(fieldName, type);
    }

    public final BaseInfo getBeanGetter(CharSequence fieldName) throws BeanException {
        return beanInfo.getBeanGetter(fieldName);
    }

    public MethodInfo getPublicIndexedGetter(CharSequence property, Class<?> indexType) {
        return beanInfo.getPublicIndexedGetter(property, indexType);
    }

    public MethodInfo getPublicSetter(CharSequence name, Class<?> preferredParamType) {
        return beanInfo.getPublicSetter(name, preferredParamType);
    }
}
