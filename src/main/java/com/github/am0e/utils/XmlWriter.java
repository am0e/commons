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
package com.github.am0e.utils;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Stack;

public class XmlWriter {
    private Writer w;
    private boolean open;
    private boolean encodeNonAscii;
    private Stack<String> stack;

    public XmlWriter(Writer w) {
        this.w = w;
        stack = new Stack<>();
    }

    /**
     * Flag to encode all characters above 127 as "&#nnnn;" where nnnn is the
     * character code.
     */
    public final void setEncodeNonAscii(boolean v) {
        encodeNonAscii = v;
    }

    public Writer getWriter() throws IOException {
        if (open)
            closeElement();
        return w;
    }

    public Writer setWriter(Writer writer) throws IOException {
        Writer prev = getWriter();
        this.w = writer;
        return prev;
    }

    // close off the opening tag
    public void closeElement() throws IOException {
        if (open) {
            open = false;
            w.append('>');
        }
    }

    public final void attributes(Map<String, Object> attributes) throws IOException {
        for (String arg : attributes.keySet()) {
            Object val = attributes.get(arg);
            if (val != null)
                attribute(arg, attributes.get(arg).toString());
        }
    }

    public final void attribute(String name, Object value) throws IOException {
        w.append(' ');
        w.append(name);
        w.append('=');
        w.append('"');
        if (value != null)
            encode(value.toString(), w, true, encodeNonAscii);
        w.append('"');
    }

    public final void addNameSpaceDecl(String name, Object value) throws IOException {
        w.append(' ');
        w.append("xmlns");
        if (name != null) {
            w.append(':');
            w.append(name);
        }
        w.append('=');
        w.append('"');
        encode(value.toString(), w, true, encodeNonAscii);
        w.append('"');
    }

    public final void attribute(String prefix, String name, Object value) throws IOException {
        w.append(' ');
        w.append(prefix);
        w.append(':');
        w.append(name);
        w.append('=');
        w.append('"');
        if (value != null)
            encode(value.toString(), w, true, encodeNonAscii);
        w.append('"');
    }

    public final void writeEmptyElement(String name) throws IOException {
        if (open)
            closeElement();
        w.append('<');
        w.append(name);
        w.append('/');
        w.append('>');
    }

    /**
     * Renders a tag open: "&lt;tag "
     * 
     * @param tagName
     * @throws IOException
     */
    public final void startElement(String name) throws IOException {
        if (open)
            closeElement();
        w.append('<');
        w.append(name);
        stack.add(name);
        open = true;
    }

    public final void startElement(String prefix, String name) throws IOException {
        String s = prefix.concat(":").concat(name);
        startElement(s);
    }

    public final void startElement(String name, Object... attrs) throws IOException {
        startElement(name);

        for (int i = 0; i < attrs.length; i += 2) {
            attribute(attrs[i].toString(), attrs[i + 1]);
        }
    }

    /**
     * Renders a close tag. The tag end is always written as "</end>"
     */
    public final void forceEndElement() throws IOException {
        if (open)
            closeElement();
        endElement();
    }

    /**
     * Renders a close tag: " tag/&gt;"
     * 
     * @param tagName
     * @throws IOException
     */
    public final void endElement() throws IOException {
        if (stack.empty()) {
            throw new IOException("Called closeTag() too many times");
        }

        String name = stack.pop();

        if (open) {
            w.append('/');
            w.append('>');
            open = false;
        } else {
            w.append('<');
            w.append('/');
            w.append(name);
            w.append('>');
        }
    }

    /**
     * Outputs the text encoding special characters.
     * 
     * @param text
     * @throws IOException
     */
    public final void text(CharSequence text) throws IOException {
        if (open)
            closeElement();
        encode(text, w, true, encodeNonAscii);
    }

    /**
     * Outputs the text with no encoding of characters. Allows the text to
     * contain embedded html tags. Unlike writeRaw(), this method will ensure
     * that a tag is closed before writing the text.
     * 
     * @param text
     * @throws IOException
     */
    public final void rawText(CharSequence text) throws IOException {
        if (open)
            closeElement();
        w.append(text);
    }

    public final void writeRaw(CharSequence data) throws IOException {
        w.append(data);
    }

    public final void encoding(String version, String encoding) throws IOException {
        w.append("<?xml version=\"");
        w.append(version);
        w.append("\" encoding=\"");
        w.append(encoding);
        w.append("\"?>");
    }

    /**
     * Ouputs the tag with the specified text.
     * 
     * @param name
     * @param text
     * @throws IOException
     */
    public final void elementWithText(String name, Object text) throws IOException {
        startElement(name);
        if (text != null)
            text(text.toString());
        endElement();
    }

    public final void elementWithText(String prefix, String name, Object text) throws IOException {
        startElement(prefix, name);
        if (text != null)
            text(text.toString());
        endElement();
    }

    public static String encode(CharSequence text, boolean encodeQuotes, boolean encodeNonAscii) {
        try {
            StringBuilder sb = new StringBuilder();
            encode(text, sb, encodeQuotes, encodeNonAscii);
            return sb.toString();

        } catch (IOException ex) {
            return "";
        }
    }

    public static void encode(CharSequence text, Appendable w, boolean encodeQuotes, boolean encodeNonAscii)
            throws IOException {
        if (text == null)
            return;

        for (int i = 0; i != text.length(); i++) {
            char c = text.charAt(i);
            if (c == '£') // pound
                w.append("&#163;");
            else if (c == '\u20AC') // euro
                w.append("&#8364;");
            else if (c == '&')
                w.append("&amp;");
            else if (c == '<')
                w.append("&lt;");
            else if (c == '>')
                w.append("&gt;");
            else if (c == '"' && encodeQuotes)
                w.append("&quot;");
            else if (c == '\'' && encodeQuotes)
                w.append("&apos;");
            else if (c >= 127 && encodeNonAscii) {
                w.append("&#");
                w.append(Integer.toString(c));
                w.append(';');
            } else {
                w.append(c);
            }
        }
    }

    /**
     * flushes this writer, throws an exception if there are as yet unclosed
     * tags.
     */
    public void flushAndValidate() throws IOException {
        w.flush();

        if (!stack.empty()) {
            throw new IOException("Tags are not all closed. Possibly, " + stack.pop() + " is unclosed. ");
        }
    }

    /**
     * This flushes and then closes the underlying stream.
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        flushAndValidate();
        w.close();
    }

    public void flush() throws IOException {
        closeElement();
        w.flush();
    }

    public void newLine() throws IOException {
        rawText("\n");
    }

    public void cdata(String text) throws IOException {
        startCDATA();
        rawText(text);
        endCDATA();
    }

    public void startCDATA() throws IOException {
        rawText("<![CDATA[");
    }

    public void endCDATA() throws IOException {
        rawText("]]>");
    }
}