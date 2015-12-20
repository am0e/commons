package com.github.am0e.json;

import java.io.IOException;
import java.util.Date;
import java.util.List;

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

import gnu.trove.stack.array.TIntArrayStack;

/**
 * Fast and simple JSON stream writer. Wraps a Writer to output a JSON object
 * stream. No intermediate objects are created - writes are immediate to the
 * underlying stream. Quoted and correct JSON encoding is performed on string
 * values, - encoding is not performed on key names - it is assumed they are
 * simple strings. The developer must call JSONWriter.encodeJSONString() on the
 * key name if required.
 * 
 * @author Kevin Roast
 */
public class JsonWriter {
    protected final Appendable out;
    protected final TIntArrayStack stack = new TIntArrayStack();

    /**
     * Constructor
     * 
     * @param out
     *            The Writer to immediately append values to (no internal
     *            buffering)
     */
    public JsonWriter(Appendable out) {
        this.out = out;
        stack.push(0);
    }

    /**
     * Start an array structure, the endArray() method must be called later.
     * NOTE: Within the array, either output objects or use the single arg
     * writeValue() method.
     */
    public void startArray() throws IOException {
        comma();
        stack.push(1);
        out.append('[');
    }

    /**
     * End an array structure.
     */
    public void endArray() throws IOException {
        out.append(']');
        stack.pop();
    }

    /**
     * Start an object structure, the endObject() method must be called later.
     */
    public void startObject() throws IOException {
        comma();
        stack.push(2);
        out.append('{');
    }

    /**
     * End an object structure.
     */
    public void endObject() throws IOException {
        out.append('}');
        stack.pop();
    }

    /**
     * Output a JSON string name and value pair.
     */
    public void write(String name, String value) throws IOException {
        write(name, value, true);
    }

    /**
     * Output a JSON number name and value pair.
     */
    public void write(String name, int value) throws IOException {
        write(name, Integer.toString(value), false);
    }

    /**
     * Output a JSON number name and value pair.
     */
    public void write(String name, float value) throws IOException {
        write(name, Float.toString(value), false);
    }

    /**
     * Output a JSON boolean name and value pair.
     */
    public void write(String name, boolean value) throws IOException {
        write(name, Boolean.toString(value), false);
    }

    public void write(String name, Object value) throws IOException {
        write(name, value, (value instanceof CharSequence) || (value instanceof Date) ? true : false);
    }

    /**
     * Output a JSON name and value pair.
     */
    public void write(String name, Object value, boolean quoted) throws IOException {
        comma();
        out.append('"');
        out.append(name);
        out.append('"');
        out.append(':');
        out.append(' ');
        write(value, quoted);
    }

    private void comma() throws IOException {
        if (stack.pop() == 4)
            out.append(", ");
        stack.push(4);
    }

    /**
     * Start a value (outputs just a name key), the endValue() method must be
     * called later. NOTE: follow with an array or object only.
     */
    public void startValue(String name) throws IOException {
        comma();
        stack.push(3);
        out.append('"');
        out.append(name);
        out.append("\": ");
    }

    /**
     * Start a named object. the endObject() method must be called later.
     */
    public void startObject(String name) throws IOException {
        startValue(name);
        startObject();
    }

    /**
     * Start a named array. the endArray() method must be called later.
     */
    public void startArray(String name) throws IOException {
        startValue(name);
        startArray();
    }

    /**
     * End a value that was started with startValue()
     */
    public void endValue() {
        stack.pop();
    }

    /**
     * Output a JSON string value. NOTE: no name is written - call from within
     * an array structure.
     */
    public void value(String value) throws IOException {
        value(value, true);
    }

    /**
     * Output a JSON number value. NOTE: no name is written - call from within
     * an array structure.
     */
    public void value(int value) throws IOException {
        value(Integer.toString(value), false);
    }

    /**
     * Output a JSON number value. NOTE: no name is written - call from within
     * an array structure.
     */
    public void value(float value) throws IOException {
        value(Float.toString(value), false);
    }

    /**
     * Output a JSON boolean value. NOTE: no name is written - call from within
     * an array structure.
     */
    public void value(boolean value) throws IOException {
        value(Boolean.toString(value), false);
    }

    /**
     * Output a JSON boolean value. NOTE: no name is written - call from within
     * an array structure.
     */
    public void value(Object value, boolean quoted) throws IOException {
        comma();
        write(value, quoted);
    }

    private void write(Object value, boolean quoted) throws IOException {
        if (value == null)
            out.append("null");
        else if (quoted) {
            out.append('\"');
            writeJsonUtf8String(value.toString());
            out.append('\"');
        } else if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) value;
            stack.push(1);
            out.append("[");
            for (int i = 0; i != list.size(); i++) {
                value(list.get(i), false);
            }
            endArray();
        } else {
            out.append(value.toString());
        }
    }

    /**
     * Write out special characters "\b, \f, \t, \n, \r", as such, backslash as
     * \\ quote as \" and values less than an ASCII space (20hex) as "\\u00xx"
     * format, characters in the range of ASCII space to a '~' as ASCII, and
     * anything higher in UTF-8.
     * 
     * @param s
     *            String to be written in utf8 format on the output stream.
     * @throws IOException
     *             if an error occurs writing to the output stream.
     */
    private void writeJsonUtf8String(String s) throws IOException {
        int len = s.length();

        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);

            if (c < ' ') { // Anything less than ASCII space, write either in
                           // \\u00xx form, or the special \t, \n, etc. form
                if (c == '\b') {
                    out.append("\\b");
                } else if (c == '\t') {
                    out.append("\\t");
                } else if (c == '\n') {
                    out.append("\\n");
                } else if (c == '\f') {
                    out.append("\\f");
                } else if (c == '\r') {
                    out.append("\\r");
                } else {
                    String hex = Integer.toHexString(c);
                    out.append("\\u");
                    int pad = 4 - hex.length();
                    for (int k = 0; k < pad; k++) {
                        out.append('0');
                    }
                    out.append(hex);
                }
            } else if (c == '\\' || c == '"') {
                out.append('\\');
                out.append(c);
            } else { // Anything else - write in UTF-8 form (multi-byte encoded)
                     // (OutputStreamWriter is UTF-8)
                out.append(c);
            }
        }
    }

    public void writeRaw(String rawText) throws IOException {
        out.append(rawText);
    }
}