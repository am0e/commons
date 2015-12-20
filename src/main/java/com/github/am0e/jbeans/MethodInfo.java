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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

final public class MethodInfo implements BaseInfo {
    /**
     * The method.
     */
    final Method method;

    /**
     * The name, if the method is a getter or setter, this will contain the
     * corresponding field name, eg "name" for the method setName().
     */
    final String name;

    /**
     * Method name
     */
    final String methodName;

    /**
     * For setters this is the parameter type of the input parameter. For
     * Methods that take a single parameter, this will contain the parameter
     * type.
     */
    final Class<?> paramType;

    /**
     * name hash code.
     */
    final int nameHash;

    /**
     * Method name hash code.
     */
    final int methodNameHash;

    /**
     * The type of method, "s" for setter, "g" for getter, "i" for indexed
     * getter and "m" for a normal method.
     */
    final byte methodType;

    /**
     * Number of arguments.
     */
    final byte nparams;

    volatile MethodHandle handle;
    // volatile MethodHandle getter;
    // volatile MethodHandle setter;

    final static public MethodInfo[] EMPTY_ARRAY = new MethodInfo[0];

    public MethodInfo(Method e) {
        String name = e.getName();
        Class<?>[] paramTypes = e.getParameterTypes();
        int nargs = paramTypes.length;
        Class<?> paramType = null;
        byte methodType = 0;
        String propName = null;

        if (nargs <= 1 && name.length() >= 3) {

            if (name.startsWith("set") && name.length() >= 4) {
                if (nargs == 1) {
                    // setProperty(String index) eg object.property = value
                    //
                    propName = Inflector.fieldFromMethodName(name, 3).intern();
                    methodType = 's';
                    paramType = paramTypes[0];
                }

            } else if (name.startsWith("get") && name.length() >= 4) {
                propName = Inflector.fieldFromMethodName(name, 3).intern();
                if (nargs == 1) {
                    // getProperty(String index) eg object.property['index']
                    //
                    methodType = 'i';
                    paramType = paramTypes[0];

                } else {
                    // getProperty() eg object.property
                    //
                    methodType = 'g';
                    paramType = e.getReturnType();
                }

            } else if (name.startsWith("is")) {
                if (nargs == 0) {
                    // isProperty() eg object.property
                    //
                    propName = Inflector.fieldFromMethodName(name, 2).intern();
                    methodType = 'g';
                    paramType = e.getReturnType();
                }
            }
        }

        if (methodType == 0) {
            // methodcall(arg1...argN) eg method(1,2,3)
            //
            methodType = 'm';
            propName = name.intern();
            paramType = (nargs == 1 ? paramTypes[0] : null);
        }

        if (Modifier.isPublic(e.getModifiers())) {
            e.setAccessible(true);
        }

        this.name = propName;
        this.methodType = methodType;
        this.paramType = paramType == null ? null : BeanUtils.getNonPrimitiveClass(paramType);
        this.method = e;
        this.nparams = (byte) nargs;
        this.nameHash = this.name.hashCode();
        this.methodName = e.getName();
        this.methodNameHash = getMethodName().hashCode();
    }

    public boolean isGetter() {
        return methodType == 'g';
    }

    public boolean isSetter() {
        return methodType == 's';
    }

    public String toString() {
        return method.toString();
    }

    public String getName() {
        return name;
    }

    public String getMethodName() {
        return methodName;
    }

    public void callSetter(Object bean, Object value) throws BeanException {
        invoke(bean, BeanUtils.cast(value, paramType));
    }

    public Object callGetter(Object bean) {
        return invoke(bean);
    }

    public Object invoke(Object obj, Object... args) throws BeanException {

        try {
            return method.invoke(obj, args);

        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new BeanException(BeanUtils.fmtExcStr("", obj, method), e);

        } catch (InvocationTargetException e) {
            throw BeanUtils.wrapError(e.getCause());
        }
    }

    /**
     * Returns the number of parameters that this method takes.
     */
    public int getParameterCount() {
        return nparams;
    }

    public Type[] getGenericParameterTypes() {
        return method.getGenericParameterTypes();
    }

    public Class<?>[] getParameterTypes() {
        return method.getParameterTypes();
    }

    /**
     * For setters this is the parameter type of the input parameter. For
     * Methods that take a single parameter, this will contain the parameter
     * type.
     */
    public Class<?> getType() {
        return paramType;
    }

    public Class<?> getActualType() {
        return paramType;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> a) {
        return method.isAnnotationPresent(a);
    }

    public <T extends Annotation> T getAnnotation(Class<T> a) {
        return method.getAnnotation(a);
    }

    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    public MethodHandle getHandle(Lookup lookup) throws IllegalAccessException {
        if (handle == null) {
            handle = lookup.unreflect(method);
        }
        return handle;
    }

    @Override
    public MethodHandle getHandle(Lookup lookup, boolean setter) {
        /*
         * try { if (setter) { if (this.setter==null) { Class<?> type =
         * BeanUtils.getPrimitiveClass(paramType); if (type==null) type =
         * paramType;
         * 
         * MethodHandle mh = lookup.findVirtual(method.getDeclaringClass(),
         * getMethodName(), MethodType.methodType(void.class, type));
         * this.setter = mh; return mh; } return this.setter;
         * 
         * } else { if (this.getter==null) { Class<?> type =
         * BeanUtils.getPrimitiveClass(paramType); if (type==null) type =
         * paramType;
         * 
         * MethodHandle mh = lookup.findVirtual(method.getDeclaringClass(),
         * getMethodName(), MethodType.methodType(type)); this.getter = mh;
         * return mh; } return this.getter;
         * 
         * } } catch (NoSuchMethodException | IllegalAccessException e) { throw
         * new BeanException(e); }
         */
        return null;
    }

    @Override
    public String makeSignature(StringBuilder sb) {
        sb.setLength(0);
        sb.append(getReturnType().toString());
        sb.append(' ');
        sb.append(getMethodName());
        sb.append('(');
        for (Class<?> type : getParameterTypes()) {
            sb.append('+');
            sb.append(type.toString());
        }
        sb.append(')');
        return sb.toString();
    }
}