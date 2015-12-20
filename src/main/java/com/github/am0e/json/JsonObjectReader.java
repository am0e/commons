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
package com.github.am0e.json;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.github.am0e.jbeans.BeanInfo;
import com.github.am0e.jbeans.BeanUtils;
import com.github.am0e.jbeans.FieldInfo;
import com.github.am0e.lib.AntLib;

/**
 * Simple object serializer to unserialize a java object from a json stream.
 * Uses javax.json
 * 
 * @author anthony
 *
 */
public class JsonObjectReader {
    private ClassLoader classLoader;
    private JsonParser parser;
    private Event ev;
    private IdentityHashMap<Class<? extends Object>, InstanceCreator<? extends Object>> instanceCreators;
    private String typeFld = JsonObjectWriter.TYPE_FLD;

    public JsonObjectReader() {
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    public void registerAdaptor(Class<? extends Object> type, Object obj) {
        if (obj instanceof InstanceCreator) {
            if (instanceCreators == null) {
                instanceCreators = new IdentityHashMap<>();
            }
            instanceCreators.put(type, (InstanceCreator<?>) obj);
        }
    }

    public static Map<String, Object> asMap(Reader r) {
        JsonObjectReader in = new JsonObjectReader();
        return in.readMap(r, null);
    }

    public static Map<String, Object> asMap(String s) {
        JsonObjectReader in = new JsonObjectReader();
        return in.readMap(new StringReader(s), null);
    }

    public static <T> T asObject(Reader r, Class<T> type) {
        JsonObjectReader in = new JsonObjectReader();
        return in.readObject(r, type);
    }

    public static <T> T asObject(String s, Class<T> type) {
        JsonObjectReader in = new JsonObjectReader();
        return in.readObject(new StringReader(s), type);
    }

    public Map<String, Object> readMap(Reader r, Class<?> itemType) {
        start(r);
        return readMap(itemType);
    }

    public <T> T readObject(Reader r, Class<T> type) {
        start(r);
        return readObject(type);
    }

    private void start(Reader r) {
        this.parser = Json.createParser(r);
        this.ev = null;
        next();
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Object readObject() {
        return readObject(null);
    }

    private Map<String, Object> readMap(Class<?> itemType) {
        assert ev == Event.START_OBJECT;

        Map<String, Object> map = AntLib.newHashMap();

        next();
        while (ev != Event.END_OBJECT) {
            String name = parser.getString();
            next();
            Object o = readObject(itemType, null);
            map.put(name, o);
        }

        next();
        return map;
    }

    @SuppressWarnings("unchecked")
    private Object readColl(Class<?> collType, Class<?> itemType) {
        assert ev == Event.START_ARRAY;

        Collection<Object> col = null;
        boolean collectionObj = false;

        // Default to a list of no collection type.
        //
        if (collType == null) {
            collType = List.class;
        }

        if (collType.isInterface()) {
            if (collType == List.class || collType == Collection.class) {
                col = AntLib.newList();

            } else if (collType == Set.class) {
                col = AntLib.newHashSet();
            }
        } else {
            // Is the collection type a class that implements Iterable.
            // If so, we have to create a default collection for the type and
            // then invoke the constructor:
            // class PhoneNumbers implements Iterable<String> {
            // PhoneNumbers(Collection col) { this.numbers = col; }
            //
            if (Iterable.class.isAssignableFrom(collType)) {
                // Get itemType from Iterable<itemType>
                //
                itemType = getCollectionObjItemClass(collType);
                col = AntLib.newList();
                collectionObj = true;
            } else {
                col = (Collection<Object>) BeanUtils.newInstance(collType);
            }
        }

        next();
        while (ev != Event.END_ARRAY) {
            Object o = readObject(itemType, null);
            col.add(o);
        }

        next();

        if (collectionObj) {
            // CollectionObj: create an instance of the class and call the
            // constructor:
            // PhoneNumbers(Collection col) { this.numbers = col; }
            //
            return BeanUtils.newInstance(collType, new Class<?>[] { Collection.class }, new Object[] { col });

        } else {
            return col;
        }
    }

    private Class<?> getCollectionObjItemClass(Class<?> collType) {
        // Get the declared interfaces.
        //
        for (Type it : collType.getGenericInterfaces()) {
            if (it instanceof ParameterizedType) {
                ParameterizedType t = (ParameterizedType) it;
                if (t.getRawType() == Iterable.class) {
                    return (Class<?>) t.getActualTypeArguments()[0];
                }
            }
        }
        // TODO Auto-generated method stub
        return null;
    }

    private Object[] readArray(Class<?> arrayType) {

        // Get the array using the component type for each item.
        //
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) readColl(List.class, arrayType.getComponentType());

        // We cannot return an object[] array. It has to be type[].
        //
        return BeanUtils.asArray(list, arrayType);
    }

    private void next() {
        if (ev == Event.END_ARRAY || ev == Event.END_OBJECT) {
            if (parser.hasNext() == false)
                return;
        }

        ev = parser.next();
    }

    @SuppressWarnings("unchecked")
    private <T> T readObject(Class<T> type) {
        BeanInfo beanInfo = null;

        // Create an instanceof claz.
        //
        T object = null;
        InstanceCreator<T> ctor = null;

        assert ev == Event.START_OBJECT;

        next();

        while (ev != Event.END_OBJECT) {
            String name = parser.getString();
            next();

            // Special processing for _type_
            //
            if (name.equals(typeFld)) {
                if (type == null) {
                    String clazName = parser.getString();
                    type = (Class<T>) loadClass(clazName);
                }
                next();
                continue;
            }

            if (object == null) {
                beanInfo = BeanInfo.forClass(type);

                if (instanceCreators != null) {
                    ctor = (InstanceCreator<T>) instanceCreators.get(type);
                }

                if (ctor == null) {
                    // Create an instanceof claz.
                    //
                    object = BeanUtils.newInstance(type);
                } else {
                    object = (T) ctor.createInstanceOf(type);
                }
            }

            // Get the field from the bean.
            //
            FieldInfo fld = beanInfo.getPublicField(name);
            if (fld == null) {
                readObject((Class<?>) null, (Class<?>) null);
            } else {
                Object value = readObject(fld.getType(), fld.getActualType());
                fld.callSetter(object, value);
            }
        }

        next();

        if (ctor != null) {
            object = ctor.onDeserialized(object);
        }

        return object;
    }

    protected Class<?> loadClass(String clazName) {
        return BeanUtils.loadClass(classLoader, clazName);
    }

    private Object readObject(Class<?> paramType, Class<?> itemType) {

        Object o;

        if (ev == Event.START_ARRAY) {
            if (paramType != null && paramType.isArray()) {
                o = readArray(itemType);
            } else {
                o = readColl(paramType, itemType);
            }

        } else if (ev == Event.START_OBJECT) {
            if (paramType == Map.class) {
                o = readMap(itemType);
            } else {
                o = readObject(paramType);
            }

        } else if (ev == Event.VALUE_NULL) {
            o = null;
            next();

        } else if (ev == Event.VALUE_NUMBER || ev == Event.VALUE_STRING) {
            o = parser.getString();
            if (paramType != null) {
                o = BeanUtils.cast(o, paramType);
            }
            next();

        } else if (ev == Event.VALUE_FALSE) {
            o = Boolean.FALSE;
            next();

        } else if (ev == Event.VALUE_TRUE) {
            o = Boolean.TRUE;
            next();

        } else {
            o = null;
        }

        return o;
    }
}
