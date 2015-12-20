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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.stream.JsonGenerator;

import com.github.am0e.functions.CustomSerializer;
import com.github.am0e.jbeans.BeanInfo;
import com.github.am0e.jbeans.FieldInfo;
import com.github.am0e.lib.AntLib;

import gnu.trove.stack.array.TIntArrayStack;

/**
 * Simple object serializer to a json stream. Uses javax.json
 * 
 * @author anthony
 *
 */
public class JsonObjectWriter {
    private JsonGenerator gen;
    private boolean includeMetaType;
    private String typeFieldName = TYPE_FLD;
    private String target = "";
    private String _name;
    private StringBuilder namePath = new StringBuilder();
    private String filterContextPath = null;
    private boolean includePackageName = false;
    public final static String TYPE_FLD = "_jt$";
    private Set<String> only;
    private Set<String> include;
    private Set<String> except;
    private IdentityHashMap<String, ObjectWriter<Object>> objectWriters;
    private IdentityHashMap<Class<?>, String> typeNames = new IdentityHashMap<>();
    protected final TIntArrayStack stack = new TIntArrayStack();
    private boolean filtering;
    private int topLevel = 1;

    public JsonObjectWriter() {
    }

    @SuppressWarnings("unchecked")
    public <T> JsonObjectWriter registerAdaptor(Class<T> type, ObjectWriter<T> obj) {
        if (objectWriters == null) {
            objectWriters = new IdentityHashMap<>();
        }
        objectWriters.put(type.getName(), (ObjectWriter<Object>) obj);
        return this;
    }

    /**
     * Except fields to exclude. For example "title,tracks.title"
     * 
     * @param fields
     * @return
     */
    public JsonObjectWriter except(String... fields) {
        if (this.except == null) {
            this.except = AntLib.newHashSet();
        }
        addFields(this.except, fields);
        return this;
    }

    private void addFields(Set<String> list, String[] fields) {
        // Fields:
        // "tags.artist.name"
        // add:
        // "tags"
        // "tags.artists"
        // "tags.artists.name"
        //
        for (String fld : fields) {
            for (int start = 0;;) {
                int end = fld.indexOf('.', start);
                if (end == -1) {
                    break;
                }
                list.add(fld.substring(0, end));
                start = end + 1;
            }
            list.add(fld);
        }
        this.filtering = true;
    }

    /**
     * Only include the specified fields.
     * 
     * For example "title,tracks.title"
     * 
     * @param fields
     * @return
     */
    public JsonObjectWriter only(String... fields) {
        if (this.only == null) {
            this.only = AntLib.newHashSet();
        }
        addFields(this.only, fields);
        return this;
    }

    /**
     * Include fields. Used for relationships. A class adaptor needs to be
     * registered with this json object writer that will call
     * {@link #includeRelation(String)}
     * 
     * For example "tracks".
     * 
     * @param fields
     * @return
     */
    public JsonObjectWriter include(String... fields) {
        if (this.include == null) {
            this.include = AntLib.newHashSet();
        }
        addFields(this.include, fields);
        return this;
    }

    public void typeName(Class<?> claz, String typeName) {
        typeNames.put(claz, typeName);
    }

    public boolean includeField(String fn) {
        String key = getIncludeKey(fn);

        if (key == null) {
            return true;
        }
        if (this.except != null && this.except.contains(key)) {
            return false;
        }
        if (this.only != null) {
            return this.only.contains(key);
        }
        return true;
    }

    public boolean includeRelation(String fn) {
        String key = getIncludeKey(fn);

        if (key == null) {
            return true;
        }
        if (this.include != null && this.include.contains(key)) {
            return true;
        }
        return false;
    }

    private String getIncludeKey(String fn) {
        if (filtering == false)
            return null;

        int len = namePath.length();

        namePath.append(fn);
        String key = namePath.toString();
        namePath.setLength(len);

        if (filterContextPath != null) {
            if (key.startsWith(filterContextPath)) {
                // Remove context path from key.
                // Eg: "data.tracks.title" becomes "tracks.title"
                //
                key = key.substring(filterContextPath.length());
            } else {
                // Not in the context path. These fields do not participate in
                // the exclusion/inclusion rules.
                // Eg. This will typically be system related data like status
                // codes or navigational links.
                //
                return null;
            }
        }

        return key;
    }

    /**
     * Set target for the json. This is typically from the client request as in
     * "/api/albums/-1233?target=list". The client is specifying that the target
     * for the data will be a list. A custom class adaptor can respond to this
     * request by returning back a custom version of the albums object.
     * 
     * @param target
     * @return
     */
    public JsonObjectWriter setTarget(String target) {
        this.target = target;
        return this;
    }

    public String getTarget() {
        return target;
    }

    public boolean isTarget(String target) {
        return this.target.equals(target);
    }

    public JsonObjectWriter setTypeFieldName(String fldName) {
        this.typeFieldName = fldName;
        return this;
    }

    public JsonObjectWriter includeTypeInfo() {
        this.includeMetaType = true;
        return this;
    }

    public JsonObjectWriter includePackageName() {
        this.includePackageName = true;
        return this;
    }

    public JsonGenerator start(Writer w) {
        gen = Json.createGenerator(w);
        namePath.setLength(0);
        stack.clear();
        return this.gen;
    }

    public void flush() {
        gen.flush();
    }

    public void write(Writer w, Object obj) {
        try {
            start(w);
            genValue(null, obj);
            flush();

        } catch (IOException e) {
            throw new JsonException(null, e);
        }
    }

    public String write(Object obj) {
        StringWriter sw = new StringWriter();
        write(sw, obj);
        return sw.toString();
    }

    public void genValue(Object v) throws IOException {
        genValue(null, v);
    }

    public void genValue(String name, Object v) throws IOException {

        if (v == null)
            return;

        // ' not working for nested'
        // http://127.0.0.1:8080/ws/tracks?fields=title,tags.artists

        if (name != null && !includeField(name) && stack.size() > 1)
            return;

        if (v instanceof Double) {
            if (name == null)
                gen.write((Double) v);
            else
                gen.write(name, (Double) v);
        } else if (v instanceof Float) {
            if (name == null)
                gen.write((Float) v);
            else
                gen.write(name, (Float) v);
        } else if (v instanceof Number) {
            if (name == null)
                gen.write(((Number) v).longValue());
            else
                gen.write(name, ((Number) v).longValue());
        } else if (v instanceof String) {
            if (name == null)
                gen.write((String) v);
            else
                gen.write(name, (String) v);
        } else if (v instanceof Boolean) {
            if (name == null)
                gen.write((Boolean) v);
            else
                gen.write(name, (Boolean) v);
        } else if (v instanceof Date) {
            long dv = ((Date) v).getTime();
            if (name == null)
                gen.write(dv);
            else
                gen.write(name, dv);
        } else if (v instanceof Path) {
            if (name == null)
                gen.write(v.toString());
            else
                gen.write(name, v.toString());
        } else {
            genObject(name, v);
        }
    }

    public void startArray() {
        if (_name == null) {
            gen.writeStartArray();
        } else {
            gen.writeStartArray(_name);
        }

        pushField();
    }

    public void startObj(String name) {
        _name = name;
        startObj();
    }

    public void startObj() {
        if (_name == null) {
            gen.writeStartObject();
        } else {
            gen.writeStartObject(_name);
        }

        pushField();
    }

    private void pushField() {
        if (filtering) {
            if (_name == null) {
                stack.push(-1);
            } else {
                stack.push(namePath.length());
                if (stack.size() > topLevel) {
                    namePath.append(_name);
                    namePath.append(".");
                }
            }
        }
        _name = null;
    }

    private void popField() {
        if (filtering) {
            int v = stack.pop();
            if (v >= 0) {
                this.namePath.setLength(v);
                System.out.println("pop " + this.namePath);
            }
        }
    }

    public void endObj() {
        popField();
        gen.writeEnd();
    }

    public void endArray() {
        popField();
        gen.writeEnd();
    }

    @SuppressWarnings("unchecked")
    public void genObject(String name, Object v) throws IOException {

        _name = name;

        // Check for custom serializer for the class.
        //
        if (objectWriters != null) {
            ObjectWriter<Object> w = objectWriters.get(v.getClass().getName());
            if (w != null) {
                w.writeObject(this, v);
                return;
            }
        }

        if (v instanceof CustomSerializer) {
            // Custom serializer.
            // The serializer can choose which fields to write to the json
            // serializer.
            // It can call JsonObjectWriter.genObjectFields() to call default
            // processing.
            //
            ((CustomSerializer<JsonObjectWriter>) v).serialize(this);
            return;
        }

        if (v instanceof Map) {
            // map.
            //
            startObj();
            genMap(v);
            endObj();

        } else if (v.getClass().isArray()) {
            // An array.
            //
            startArray();
            genArray(v);
            endArray();

        } else if (v instanceof Iterable<?>) {
            // Collection
            //
            startArray();
            genIter((Iterable<?>) v);
            endArray();

        } else if (v instanceof Iterator<?>) {
            // Collection
            //
            startArray();
            genIter((Iterator<?>) v);
            endArray();

        } else {
            // POJO.
            //
            startObj();
            genObjectFields(v);
            endObj();
        }
    }

    private void genIter(Iterable<?> iter) throws IOException {
        genIter(iter.iterator());
    }

    private void genIter(Iterator<?> iter) throws IOException {
        while (iter.hasNext()) {
            genValue(iter.next());
        }
    }

    private void genArray(Object v) throws ArrayIndexOutOfBoundsException, IllegalArgumentException, IOException {
        int sz = Array.getLength(v);

        for (int i = 0; i != sz; i++) {
            genValue(Array.get(v, i));
        }
    }

    private void genMap(Object v) throws IOException {
        // Map.
        //
        Map<?, ?> map = (Map<?, ?>) v;
        for (Entry<?, ?> it : map.entrySet()) {
            Object itv = it.getValue();
            String key = (String) it.getKey();

            if (itv != null)
                genValue(key, itv);
        }
    }

    @SuppressWarnings("unchecked")
    public void genObject(Object v) throws IOException {

        if (v instanceof Map) {
            genMap(v);

        } else if (v.getClass().isArray()) {
            genArray(v);

        } else if (v instanceof Iterable<?>) {
            genIter((Iterable<?>) v);

        } else if (v instanceof Iterator<?>) {
            genIter((Iterator<?>) v);

        } else if (v instanceof CustomSerializer) {
            // Custom serializer.
            // The serializer can choose which fields to write to the json
            // serializer.
            // It can call JsonObjectWriter.genObjectFields() to call default
            // processing.
            //
            ((CustomSerializer<JsonObjectWriter>) v).serialize(this);

        } else {
            // POJO.
            //
            genObjectFields(v);
        }
    }

    public void genObjectFields(Object v) throws IOException {

        genTypeField(v);

        BeanInfo beanInfo = BeanInfo.forClass(v.getClass());

        while (beanInfo != null) {

            // Process each acccessible field in this class.
            //
            for (FieldInfo mf : beanInfo.getDeclaredPublicFields()) {
                if (mf.isReadable() && !mf.isTransient()) {
                    // Get the field value.
                    //
                    Object fv = mf.callGetter(v);

                    if (fv != null) {
                        genValue(mf.getName(), fv);
                    }
                }
            }

            // Do the super class members.
            //
            beanInfo = beanInfo.getSuperBeanInfo();
        }
    }

    /**
     * Exclude the top level field name in the filter name path.
     * <p>
     * For example:
     * 
     * <pre>
     * {
     *    data: {
     *       id: "1012",
     *       type: "Track",
     *       title: "Sonata 23 in C Minor"
     *    }
     * }
     * </pre>
     * 
     * In the above example, we will want to exclude data from the field path so
     * that the client can request the fields starting inside data. For example
     * the following url "/api/tracks/1012?only=type,id" will request the fields
     * type, id, excluding "data." from the path.
     * 
     */
    public void skipTopLevelField() {
        topLevel = 2;
    }

    public void genTypeField(Object obj) {
        if (includeMetaType) {
            String name;

            if (obj instanceof TypeFldNameProvider) {
                name = ((TypeFldNameProvider) obj).getTypeFieldName();
            } else {
                Class<?> claz = obj.getClass();

                name = typeNames.get(claz);

                if (name == null) {
                    name = includePackageName ? claz.getName() : claz.getSimpleName();
                }
            }

            if (name != null && includeField(typeFieldName)) {
                gen.write(typeFieldName, name);
            }
        }
    }

    public void setFilterContextPath(String path) {
        this.filterContextPath = path;
    }
}
