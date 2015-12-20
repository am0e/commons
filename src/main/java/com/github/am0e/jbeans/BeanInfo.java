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
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.github.am0e.lib.AntLib;

/**
 * Wrapper around the Java reflection classes.
 * 
 * @author Anthony
 */
public final class BeanInfo {
    /**
     * The class that this bean info object represents.
     */
    final Class<?> beanClass;

    /**
     * Does the class have a super class?
     */
    final boolean hasSuperClass;

    /**
     * An array of the public methods declared in the class. Methods declared in
     * the super class are not included.
     */
    final MethodInfo[] declaredMethods;

    /**
     * The super class info. Initialised when first accessed!
     */
    private volatile BeanInfo _superBeanInfo;

    /**
     * A collection of fields declared in this class. Initialised when first
     * accessed!
     */
    private volatile FieldInfo[] _declaredFields;

    /**
     * A collection of properties declared in this class. Initialised when first
     * accessed!
     */
    private volatile PropertyInfo[] _declaredProps;

    /**
     * System-wide cache of beanInfos. We assume not many updates as we are
     * caching classes not general objects.
     */
    private static ConcurrentMap<Class<?>, SoftReference<BeanInfo>> beanInfoCache = new ConcurrentHashMap<>(16, 0.75f,
            2);

    /**
     * Gets the BeanClassWrapper associated with the specified class. Returns an
     * existing BeanClassWrapper if cached, otherwise a ClassDef is created for
     * the class and stored in the classDefCache.
     * 
     * @param clss
     * @return A ClassDef.
     */
    public static BeanInfo forClass(Class<?> beanClass) {
        SoftReference<BeanInfo> ref = beanInfoCache.get(beanClass);

        BeanInfo bi = (ref == null ? null : ref.get());

        if (bi == null) {
            // Note we don't care if 2 threads get in here as only one of them
            // will replace the other's
            // entry.
            //
            BeanInfo newInfo = new BeanInfo(beanClass);

            // Cache our copy.
            // We may be competing with other threads here!!
            //
            beanInfoCache.put(beanClass, new SoftReference<BeanInfo>(newInfo));

            // Retry the cache operation to get the actual cached object.
            //
            ref = beanInfoCache.get(beanClass);

            if (ref == null || (bi = ref.get()) == null) {
                // Incase of GC. The ref in the cache may have already been
                // cleared!!
                // All we can do is return our own copy.
                //
                bi = newInfo;
            }
        }

        return bi;
    }

    public BeanInfo(Class<?> beanClass) {

        List<MethodInfo> methods = AntLib.newList();

        for (Method e : beanClass.getDeclaredMethods()) {
            if (Modifier.isPublic(e.getModifiers())) {
                methods.add(new MethodInfo(e));
            }
        }

        this.declaredMethods = methods.toArray(new MethodInfo[0]);
        this.beanClass = beanClass;

        // Does the class have a super class?
        //
        Class<?> superClaz = beanClass.getSuperclass();

        if (superClaz != null && superClaz != Object.class) {
            hasSuperClass = true;
        } else {
            hasSuperClass = false;
        }
    }

    /**
     * Find a public method declared in the bean class represented by this
     * object, excluding methods from the super class.
     * 
     * @param name
     *            The name of the method to find.
     * @param nargs
     *            The number of arguments. -1 means ignore the arguments and
     *            return the first method.
     * @return
     */
    public MethodInfo getDeclaredPublicMethod(final CharSequence name, final int nargs) {
        final int hashCode = name.hashCode();

        for (final MethodInfo m : declaredMethods) {
            if (m.methodNameHash == hashCode) {
                if ((nargs == -1 || m.nparams == nargs) && m.methodName.equals(name)) {
                    return m;
                }
            }
        }
        return null;
    }

    /**
     * Find a public method declared in the bean class represented by this
     * object, excluding methods from the super class. The number of arguments
     * are ignored.
     * 
     * @param name
     *            The name of the method to find.
     * @return
     */
    public MethodInfo getDeclaredPublicMethod(final CharSequence name) {
        return getDeclaredPublicMethod(name, -1);
    }

    public MethodInfo getDeclaredPublicIndexedGetter(final CharSequence name, final Class<?> indexType) {
        final int hashCode = name.hashCode();
        MethodInfo found = null;

        for (final MethodInfo m : declaredMethods) {
            if (m.methodType == 'i' && m.nameHash == hashCode && (m.name == name || m.name.equals(name))) {
                found = m;

                if (m.paramType == indexType)
                    return m;
            }
        }
        return found;
    }

    /**
     * Find a public setter method declared in the bean class represented by
     * this object, excluding methods from the super class.
     * 
     * @param name
     *            The field name, eg "name".
     * @return
     */
    public MethodInfo getDeclaredPublicSetter(final CharSequence name, final Class<?> preferredParamType) {
        final int hashCode = name.hashCode();
        MethodInfo found = null;

        for (final MethodInfo m : declaredMethods) {
            if (m.methodType == 's' && m.nameHash == hashCode && (m.name == name || m.name.equals(name))) {
                if (found == null)
                    found = m;

                if (m.paramType == preferredParamType)
                    return m;
            }
        }

        return found;
    }

    public FieldInfo[] getDeclaredPublicFields() {
        if (_declaredFields == null) {
            initialiseFields();
        }
        return _declaredFields;
    }

    public PropertyInfo[] getDeclaredProperties() {
        if (_declaredProps == null) {
            initialiseProps();
        }
        return _declaredProps;
    }

    private void initialiseProps() {
        List<PropertyInfo> props = AntLib.newList();

        Map<String, MethodInfo> setters = AntLib.newHashMap();
        Set<String> set = AntLib.newHashSet();

        // Get all methods that are setters.
        //
        for (final MethodInfo m : declaredMethods) {
            if (m.isSetter() && m.nparams == 1) {
                setters.put(m.name, m);
            }
        }

        // For all the getters, if the method also has the associated setter,
        // add it as a property.
        //
        for (final MethodInfo m : declaredMethods) {
            MethodInfo setter;
            if (m.isGetter() && m.nparams == 0 && (setter = setters.get(m.name)) != null) {

                // For Map/List classes we will get the parameterized type.
                // List<Type>
                // Map<String,Type>
                //
                Class<?> actualType = BeanUtils.getActualTypeFromMethodOrField(m.method, null);

                props.add(new PropertyInfo(m, setter, m.getReturnType(), actualType));
                set.add(m.name);
            }
        }

        // All public fields are gettable/settable so add them as properties if
        // not already
        // added.
        // Get all the fields, private/protected/public.
        //
        for (FieldInfo f : getDeclaredPublicFields()) {
            // Get all public fields Ignoring static fields.
            //
            if (set.contains(f.name) == false) {
                props.add(new PropertyInfo(f, f, f.getType(), f.getActualType()));
            }
        }

        _declaredProps = props.toArray(new PropertyInfo[0]);
    }

    private void initialiseFields() {
        List<FieldInfo> fields = AntLib.newList();

        // Get all the fields, private/protected/public.
        //
        for (Field field : beanClass.getDeclaredFields()) {

            // Ignoring static fields.
            //
            if (Modifier.isStatic(field.getModifiers()) == false) {

                // Get the associated setter/getter methods.
                //
                MethodInfo setter = getDeclaredPublicMethod(Inflector.getSetterMethod(field.getName()), 1);
                MethodInfo getter = null;

                // Check for:
                // Boolean isBool()
                // boolean isBool()
                //
                if (field.getType() == Boolean.class || field.getType() == Boolean.TYPE) {
                    // First try is<PropName>
                    //
                    getter = getDeclaredPublicMethod(Inflector.getIsMethod(field.getName()), 0);

                    if (getter == null) {
                        // Otherwise try <propName>
                        //
                        getter = getDeclaredPublicMethod(field.getName(), 0);
                    }

                } else {
                    // Try get<PropName>
                    //
                    getter = getDeclaredPublicMethod(Inflector.getGetterMethod(field.getName()), 0);
                }

                // We are only interested in the field if it can be accessed.
                // Ie it is public, or it has a setter and or a getter.
                //
                if (Modifier.isPublic(field.getModifiers()) || getter != null || setter != null) {
                    fields.add(new FieldInfo(field, getter, setter));
                }
            }
        }

        _declaredFields = fields.toArray(new FieldInfo[0]);

    }

    public MethodInfo[] getDeclaredPublicMethods() {
        return declaredMethods;
    }

    /**
     * Find a public field declared in the bean class represented by this
     * object, including fields from the super class.
     * 
     * @param name
     *            The name of the field to get.
     * @return
     */
    public final FieldInfo getPublicField(CharSequence name) {
        final int hashCode = name.hashCode();

        for (BeanInfo it = this; it != null; it = it.getSuperBeanInfo()) {
            for (FieldInfo m : it.getDeclaredPublicFields()) {
                if (m.hash == hashCode && (m.name == name || m.name.equals(name)))
                    return m;
            }
        }
        return null;
    }

    /**
     * Find a property declared in the bean class represented by this object,
     * including fields from the super class.
     * 
     * @param name
     *            The name of the field to get.
     * @return
     */
    public final PropertyInfo getProperty(CharSequence name) {
        final int hashCode = name.hashCode();

        for (BeanInfo it = this; it != null; it = it.getSuperBeanInfo()) {
            for (PropertyInfo m : it.getDeclaredProperties()) {
                if (m.hash == hashCode && (m.name == name || m.name.equals(name)))
                    return m;
            }
        }
        return null;
    }

    /**
     * Find a public method declared in the bean class represented by this
     * object, including methods from the super class.
     * 
     * @param name
     *            The method name.
     * @param nargs
     *            The number of arguments.
     * @return
     */
    public final MethodInfo getPublicMethod(CharSequence name, int nargs) {
        for (BeanInfo it = this; it != null; it = it.getSuperBeanInfo()) {
            MethodInfo m = it.getDeclaredPublicMethod(name, nargs);
            if (m != null)
                return m;
        }
        return null;
    }

    public MethodInfo getPublicMethod(CharSequence name) {
        return getPublicMethod(name, -1);
    }

    public final MethodInfo getPublicSetter(CharSequence fieldName, Class<?> preferredParamType) throws BeanException {
        for (BeanInfo it = this; it != null; it = it.getSuperBeanInfo()) {
            MethodInfo m = it.getDeclaredPublicSetter(fieldName, preferredParamType);
            if (m != null)
                return m;
        }
        return null;
    }

    /**
     * Find a public getter method declared in the class.
     * 
     * @param name
     *            The field name, eg "name".
     * @return
     */
    public final MethodInfo getPublicGetter(CharSequence name) {
        final int hashCode = name.hashCode();

        for (BeanInfo it = this; it != null; it = it.getSuperBeanInfo()) {
            for (final MethodInfo m : it.declaredMethods) {
                if (m.methodType == 'g' && m.nameHash == hashCode && (m.name == name || m.name.equals(name))) {
                    return m;
                }
            }
        }
        return null;
    }

    public final MethodInfo getPublicIndexedGetter(CharSequence fieldName, Class<?> indexType) {
        for (BeanInfo it = this; it != null; it = it.getSuperBeanInfo()) {
            MethodInfo m = it.getDeclaredPublicIndexedGetter(fieldName, indexType);
            if (m != null)
                return m;
        }
        return null;
    }

    public final BeanInfo getSuperBeanInfo() {
        if (hasSuperClass && _superBeanInfo == null) {
            // Get the super class.
            //
            _superBeanInfo = forClass(beanClass.getSuperclass());
        }

        return _superBeanInfo;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public String toString() {
        return beanClass.toString();
    }

    /**
     * Geta a bean getter. Either a public method or a field. The getters are
     * cached for speed of access.
     * 
     * @param name
     * @return
     */
    public BaseInfo getBeanGetter(CharSequence name) {
        BaseInfo getter = getPublicGetter(name);

        if (getter == null) {
            getter = getPublicField(name);
        }

        return getter;
    }

    public BaseInfo getBeanSetter(CharSequence name, Class<?> paramType) {
        BaseInfo setter = getPublicSetter(name, paramType);
        if (setter != null)
            return setter;

        FieldInfo fieldInfo = getPublicField(name);
        if (fieldInfo != null && fieldInfo.isSettable())
            return fieldInfo;

        return null;
    }

    public Collection<PropertyInfo> getPropertiesWithAnnotation(Class<? extends Annotation> an) {
        Map<String, PropertyInfo> props = AntLib.newHashMap(5);
        StringBuilder sb = new StringBuilder();

        for (BeanInfo beanClass = this; beanClass != null; beanClass = beanClass.getSuperBeanInfo()) {
            for (PropertyInfo it : beanClass.getDeclaredProperties()) {
                if (it.setter.isAnnotationPresent(an)) {
                    String key = it.makeSignature(sb);
                    if (props.containsKey(key) == false) {
                        props.put(key, it);
                    }
                }
            }
        }

        return props.values();
    }

    public Collection<MethodInfo> getMethodsWithAnnotation(Class<? extends Annotation> an) {
        Map<String, MethodInfo> props = AntLib.newHashMap(5);
        StringBuilder sb = new StringBuilder();

        for (BeanInfo beanClass = this; beanClass != null; beanClass = beanClass.getSuperBeanInfo()) {
            beanClass.getBeanClass().getMethods();

            for (MethodInfo it : beanClass.getDeclaredPublicMethods()) {
                if (it.isAnnotationPresent(an)) {
                    String key = it.makeSignature(sb);
                    if (props.containsKey(key) == false) {
                        props.put(key, it);
                    }
                }
            }
        }

        return props.values();
    }

    /**
     * Find a public method annotated with the given annotation in the class or
     * the super class.
     * 
     * @param an
     * @return
     */
    public MethodInfo getAnnotatedMethod(Class<? extends Annotation> an) {
        for (BeanInfo beanClass = this; beanClass != null; beanClass = beanClass.getSuperBeanInfo()) {
            for (MethodInfo it : beanClass.getDeclaredPublicMethods()) {
                if (it.isAnnotationPresent(an)) {
                    return it;
                }
            }
        }
        return null;
    }
}