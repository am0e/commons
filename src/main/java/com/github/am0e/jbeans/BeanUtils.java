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

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.am0e.msgs.Msgs;
import com.github.am0e.utils.Urls;
import com.github.am0e.utils.Validate;

/**
 * Common reflection utilities.
 * 
 * Some benchmarks for various java methods. Run in a loop of 10000000
 * iterations. getAnnotation() found 1726 getAnnotation not-found 1657
 * isInstance() 2098 isPrimitive() 1985 isAssignableFrom() 3382 instanceof
 * Operator 428 Reflection invoke 3999 MethodHandle invoke 1948
 * 
 * @author anthony
 *
 */
public final class BeanUtils {

    public static final <T> Constructor<T> getConstructor(Class<T> claz, Class<?>... parameterTypes) {

        try {
            return claz.getConstructor(parameterTypes);

        } catch (Exception e) {
            throw wrapError(e);
        }
    }

    /**
     * Creates an instance of a class.
     * 
     * @return
     * @throws BeanException
     */
    public static final <T> T newInstance(Class<T> claz) throws BeanException {
        try {
            return claz.newInstance();

        } catch (Exception e) {
            throw wrapError(e);
        }
    }

    /**
     * Creates an instance of a class.
     * 
     * @return
     * @throws BeanException
     */
    @SuppressWarnings("unchecked")
    public static final <T> Class<T> loadClass(ClassLoader cl, String className) throws BeanException {
        try {
            return (Class<T>) ClassCache.loadClass(cl, className);

        } catch (ClassNotFoundException e) {
            throw new BeanException(Msgs.format("Class Not Found {}", className));
        }
    }

    /**
     * Creates an instance of a class.
     * 
     * @return
     * @throws BeanException
     */
    public static final <T> T newInstance(Constructor<T> c, Object... args) throws BeanException {
        try {
            return c.newInstance(args);

        } catch (Exception e) {
            throw wrapError(e);
        }
    }

    /**
     * Creates an instance of a class.
     * 
     * @return
     * @throws BeanException
     */
    public static final <T extends Object> T newInstance(Class<T> claz, Class<?>[] parameterTypes, Object[] args)
            throws BeanException {
        try {
            Constructor<T> ctor = claz.getConstructor(parameterTypes);
            ctor.setAccessible(true);

            return ctor.newInstance(args);

        } catch (Exception e) {
            throw wrapError(e);
        }
    }

    public static Object invoke(Object obj, Method m, Object[] args) throws BeanException {
        try {
            return m.invoke(obj, args);

        } catch (Exception e) {
            throw wrapError(e.getCause());
        }
    }

    public static void fieldSet(Object bean, Field field, Object obj) {
        obj = cast(obj, field.getType());

        try {
            // If the field is private/public, set the value directly.
            //
            if (!field.isAccessible())
                field.setAccessible(true);

            field.set(bean, obj);

        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw BeanException.fmtExcStr("callSetter", bean, field, e);
        }
    }

    public static void copyBeanTo(Object fromBean, Object toBean) {
        BeanClassWrapper fromBCW = BeanClassWrapper.getBeanClassWrapper(fromBean.getClass());
        BeanClassWrapper toBCW = BeanClassWrapper.getBeanClassWrapper(toBean.getClass());

        // For all of the fields in the bean, bind the field values to the map.
        //
        for (PropertyInfo fromField : fromBCW.getDeclaredProperties()) {
            // Get the value from the bean corresponding to the bean field.
            //
            Object value = fromBCW.callGetter(fromBean, fromField.getName());
            if (value != null) {
                // Now if we have a value, try to copy the bean field.
                //
                toBCW.callSetter(toBean, fromField.getName(), value);
            }
        }
    }

    public static void copyBeanFieldsToMap(Object bean, Map<String, Object> map) {
        // For all of the fields in the bean, bind the field values to the map.
        //
        BeanClassWrapper cd = new BeanClassWrapper(bean.getClass());

        do {
            for (PropertyInfo it : cd.getDeclaredProperties()) {
                // Get the bean field value.
                //
                map.put(it.name, it.callGetter(bean));
            }
        } while (cd.setToSuper());
    }

    public static void setBeanFieldsFromMap(Object bean, Map<String, ?> map) {
        // For all of the fields in the bean, bind the field values to the map.
        //
        BeanClassWrapper cd = BeanClassWrapper.getBeanClassWrapper(bean.getClass());

        for (PropertyInfo it : cd.getDeclaredProperties()) {
            if (map.containsKey(it.name)) {
                // Get the value from the map corresponding to the bean field.
                //
                it.callSetter(bean, map.get(it.name));
            }
        }
    }

    public static String fmtExcStr(String msg, Object bean, Object name) {
        return msg + " Bean=" + bean.getClass().getName() + " Name=" + name;
    }

    public final static Object cast(Object value, Class<?> toType) {

        toType = getNonPrimitiveClass(toType);

        if (value == null) {
            // value is null.
            // Return zero if we can.
            //
            return zeroValue(toType);
        }

        final Class<?> valueType = value.getClass();

        // Value is same type as param type or param type is a generic object in
        // which case we dont cast
        //
        if (toType == valueType || toType == Object.class || toType.isInstance(value)) {
            return value;
        }

        // Cast to a collection.
        //
        if (Collection.class.isAssignableFrom(toType)) {
            if (value instanceof Collection) {
                return value;
            } else if (value instanceof Object[]) {
                return Arrays.asList((Object[]) value);
            } else {
                return Arrays.asList(value);
            }
        }

        if (toType.isArray()) {
            if (value instanceof List) {
                // convert from list to array.
                return asArray((List<?>) value, toType);
            } else {
                return asArray(Arrays.asList(value), toType);
            }
        }

        if (value instanceof CharSequence) {
            // Cast String to paramType
            //
            return castStringTo(value.toString(), toType);
        }

        if (value instanceof Number) {
            // Cast Number to paramType. Eg from Long to Integer
            //
            return castNumberTo((Number) value, toType);
        }

        return value;
    }

    private static Object castStringTo(String s, Class<?> toType) {
        // An empty string cannot be cast so we return null.
        //
        if (s.isEmpty()) {
            return null;
        }

        if (toType == Integer.class)
            return Integer.valueOf(s);
        if (toType == Long.class)
            return Long.valueOf(s);
        if (toType == Double.class)
            return Double.valueOf(s);
        if (toType == Short.class)
            return Short.valueOf(s);
        if (toType == Byte.class)
            return Byte.valueOf(s);
        if (toType == Float.class)
            return Float.valueOf(s);
        if (toType == Character.class)
            return s.charAt(0);
        if (toType == Boolean.class)
            return convertBoolean(s);
        if (toType == BigDecimal.class)
            return new BigDecimal(s);
        if (toType == BigInteger.class)
            return new BigInteger(s);
        if (toType == Path.class)
            return Paths.get(s);
        if (toType == URL.class)
            return Urls.toUrl(s);
        if (toType == URI.class)
            return Urls.toUri(s);
        if (toType == File.class)
            return new File(s);
        if (Date.class.isAssignableFrom(toType)) {
            return castDate(s);
        }
        if (Enum.class.isAssignableFrom(toType)) {
            return toEnum(toType, s);
        }

        return s;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final Object toEnum(Class<?> type, String value) {
        return Enum.valueOf((Class<Enum>) type, value);
    }

    public static Number castNumber(Number s, Class<?> paramType) {
        return castNumberTo(s, paramType);
    }

    private static Number castNumberTo(Number s, Class<?> paramType) {
        // Cast string to numeric / date
        //
        if (paramType == Integer.class)
            return s.intValue();
        if (paramType == Long.class)
            return s.longValue();
        if (paramType == Double.class)
            return s.doubleValue();
        if (paramType == Short.class)
            return s.shortValue();
        if (paramType == Byte.class)
            return s.byteValue();
        if (paramType == Float.class)
            return s.floatValue();
        return s;
    }

    public static Boolean convertBoolean(String s) {
        if (s == null)
            return Boolean.FALSE;

        if ((s.length() == 1 && s.charAt(0) == '1') || s.equals("true") || s.equalsIgnoreCase("true"))
            return Boolean.TRUE;

        return Boolean.FALSE;
    }

    public static Object zeroValue(Class<?> paramType) {
        if (paramType == Integer.class)
            return Integer.valueOf(0);
        if (paramType == Long.class)
            return Long.valueOf(0);
        if (paramType == Double.class)
            return Double.valueOf(0);
        if (paramType == Boolean.class)
            return Boolean.FALSE;
        if (paramType == Short.class)
            return Short.valueOf((short) 0);
        if (paramType == Byte.class)
            return Byte.valueOf((byte) 0);
        if (paramType == Float.class)
            return Float.valueOf(0);
        if (paramType == BigDecimal.class)
            return BigDecimal.ZERO;
        if (paramType == BigInteger.class)
            return BigInteger.ZERO;
        return null;
    }

    public final static Class<?> getPrimitiveClass(Class<?> type) {
        if (type == Double.class)
            return Double.TYPE;
        if (type == Long.class)
            return Long.TYPE;
        if (type == Integer.class)
            return Integer.TYPE;
        if (type == Short.class)
            return Short.TYPE;
        if (type == Boolean.class)
            return Boolean.TYPE;
        if (type == Byte.class)
            return Byte.TYPE;
        if (type == Character.class)
            return Character.TYPE;
        if (type == Float.class)
            return Float.TYPE;
        return null;
    }

    public final static Class<?> getNonPrimitiveClass(Class<?> type) {
        // Note call to Class.isPrimitive() is slow! so we use inline if
        // statements.
        //
        if (type == Double.TYPE)
            return Double.class;
        if (type == Long.TYPE)
            return Long.class;
        if (type == Integer.TYPE)
            return Integer.class;
        if (type == Short.TYPE)
            return Short.class;
        if (type == Boolean.TYPE)
            return Boolean.class;
        if (type == Byte.TYPE)
            return Byte.class;
        if (type == Character.TYPE)
            return Character.class;
        if (type == Float.TYPE)
            return Float.class;
        return type;
    }

    public final static Date castDate(String s) {
        if (s.charAt(4) == '-' && s.indexOf(':') > 0) {
            // yyyy-[m]m-[d]d hh:mm:ss[.f...].
            return Timestamp.valueOf(s);
        } else {
            return new Date(Long.parseLong(s));
        }
    }

    public static MethodInfo getMethod(Class<?> claz, String name) {
        MethodInfo method = BeanInfo.forClass(claz).getPublicMethod(name);
        return method;
    }

    public final static Class<?> getActualTypeFromMethodOrField(Method m, Field f) {

        Class<?> type = (m == null ? f.getType() : m.getReturnType());
        Class<?> actualType = type;

        // For Map/List classes we will get the parameterized type.
        // List<Type>
        // Map<String,Type>
        //
        if (type == List.class || type == Map.class || type == Set.class || type == Collection.class) {
            Type gt;

            if (m == null) {
                gt = f.getGenericType();

            } else {
                gt = m.getGenericReturnType();
            }

            if (gt instanceof ParameterizedType) {
                // Get the type arguments.
                //
                Type[] targs = ((ParameterizedType) gt).getActualTypeArguments();

                // Now get the real type.
                // List<type>
                // Map<keytype,valuetype>
                //
                if (type == List.class || type == Set.class || type == Collection.class) {
                    actualType = (Class<?>) targs[0]; // type

                } else if (type == Map.class) {
                    actualType = (Class<?>) targs[1]; // valuetype
                }
            }
        }

        return actualType;
    }

    @SuppressWarnings("unchecked")
    public final static <T extends Object> List<T> asList(Object v, Class<T> t) {

        if (v == null)
            return null;

        List<T> list;
        if (v instanceof List) {
            list = (List<T>) v;
        } else {
            list = (List<T>) Arrays.asList(v);
        }

        for (int i = 0; i != list.size(); i++) {
            list.set(i, (T) BeanUtils.cast(list.get(i), t));
        }

        return list;
    }

    public final static Object[] asArray(List<?> list, Class<?> arrayType) {
        int sz = list.size();

        if (arrayType.isArray() == false) {
            throw Validate.illegalArgument("arrayType");
        }

        // Construct a new array for the given type.
        ///
        Object[] ar = (Object[]) Array.newInstance(arrayType.getComponentType(), sz);

        // Copy values from the list into the array.
        //
        for (int i = 0; i != sz; i++) {
            ar[i] = list.get(i);
        }

        // Return the array.
        //
        return ar;
    }

    public static RuntimeException wrapError(Throwable e) {
        // Reflection invoke() exception, wrap the actual exception.
        //
        if (e instanceof InvocationTargetException) {
            e = e.getCause();
        }

        // No need to wrap a runtime exception.
        //
        if (e instanceof RuntimeException)
            return (RuntimeException) e;
        else
            return new BeanException(e);
    }

    public static BeanInfo getBeanInfo(Object obj) {
        return BeanInfo.forClass(obj.getClass());
    }
}
