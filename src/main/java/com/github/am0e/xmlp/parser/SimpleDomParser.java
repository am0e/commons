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
package com.github.am0e.xmlp.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;

import com.github.am0e.lib.AntLib;
import com.github.am0e.msgs.Msgs;
import com.github.am0e.xmlp.model.Attribute;
import com.github.am0e.xmlp.model.Attributes;
import com.github.am0e.xmlp.model.CData;
import com.github.am0e.xmlp.model.Document;
import com.github.am0e.xmlp.model.Element;
import com.github.am0e.xmlp.model.Node;
import com.github.am0e.xmlp.model.Text;

public class SimpleDomParser {
    private Map<String, NodeFactory> nodeFactories;
    private NodeFactory defaultFactory;
    private boolean internNames = true;
    private boolean ignoreEmptyText = true;
    private boolean ignoreSpace = true;
    private boolean convertLocalNamesToLowerCase = false;
    private Document document;
    private Node current;
    private Stack<StackItem> nodeStack;
    private DomParseEvents parseEvents;
    private CreateNodeCtx createCtx;
    private XMLStreamReader reader;
    private XMLInputFactory inputFactory;

    private final static NodeFactory defaultNodeFactory = new NodeFactory(null);
    private final static XMLInputFactory defaultInputFactory = XMLInputFactory.newFactory();

    final class StackItem {
        Node parent;
        boolean add;
    }

    public SimpleDomParser() {
        nodeFactories = AntLib.newHashMap();
        defaultFactory = defaultNodeFactory;
        inputFactory = defaultInputFactory;
        nodeStack = new Stack<>();
        createCtx = new CreateNodeCtx();
    }

    private DomParseException formatExc(XMLStreamException e, String name) {
        String msg = e.getLocalizedMessage();
        msg = StringUtils.substringBefore(msg, "\r");
        Location location = e.getLocation();
        if (location != null) {
            msg = msg.concat(Msgs.format("\n at ({},{})", location.getLineNumber(), location.getColumnNumber()));
        }
        if (name != null) {
            msg = msg.concat(Msgs.format("\n in ({})", name));
        }

        return new DomParseException(msg);
    }

    public Document parse(String resourceName, Reader rdr) throws IOException {
        try {
            return parse(resourceName, inputFactory.createXMLStreamReader(rdr));

        } catch (XMLStreamException e) {
            throw formatExc(e, resourceName);
        }
    }

    /**
     * Parses the input stream.
     * 
     * @param is
     * @return
     * @throws IOException
     */
    public Document parse(String name, InputStream is) throws IOException {
        return parse(name, is, "UTF8");
    }

    /**
     * Parses the input stream.
     * 
     * @param stream
     *            input stream.
     * @param encoding
     * @return
     * @throws IOException
     */
    public Document parse(String resourceName, InputStream stream, String encoding) throws IOException {
        try {
            return parse(resourceName, inputFactory.createXMLStreamReader(stream, encoding));

        } catch (XMLStreamException e) {
            throw formatExc(e, resourceName);
        }
    }

    private Document parse(String resourceName, XMLStreamReader rdr) throws IOException {
        try {
            this.document = createDocument();
            this.reader = rdr;

            inputFactory.setProperty("javax.xml.stream.isCoalescing", Boolean.TRUE);

            // Load the document.
            //
            buildTree(document);
            document.nodeInitialized();

        } catch (XMLStreamException e) {
            throw formatExc(e, resourceName);
        }

        return document;
    }

    protected Document createDocument() {
        return new Document();
    }

    protected Element createElement(CreateNodeCtx cc) throws XMLStreamException {

        NodeFactory lib = getNodeFactory(cc.namespaceUri());

        // Default to default factory.
        //
        if (lib == null) {
            lib = defaultFactory;
        }

        // Create the node from the factory for the namespace.
        //
        Element e = lib.createNode(cc);
        return e;
    }

    protected void buildTree(Node parentNode) throws XMLStreamException, IOException {

        current = parentNode;

        for (;;) {
            int evtType = reader.next();
            processEvent(evtType);

            if (evtType == XMLStreamConstants.END_DOCUMENT)
                break;
        }
    }

    protected void processEvent(int evtType) throws XMLStreamException, IOException {
        if (evtType == XMLStreamConstants.START_ELEMENT) {
            startElement();

        } else if (evtType == XMLStreamConstants.END_ELEMENT) {
            endElement();

        } else if (evtType == XMLStreamConstants.CHARACTERS) {
            Node child = createTextNode();
            if (child != null) {
                current.appendChild(child);
            }

        } else if (evtType == XMLStreamConstants.CDATA) {
            Node child = createCDATANode();
            if (child != null) {
                current.appendChild(child);
            }

        } else if (evtType == XMLStreamConstants.COMMENT) {
            comment();
        }
    }

    protected void comment() throws XMLStreamException {
    }

    protected Node createTextNode() {
        String ar2 = reader.getText();
        String cs2 = ar2;
        Node child = null;

        if (ignoreSpace)
            cs2 = trimString(ar2);

        if (cs2.length() == 0) {
            if (ignoreEmptyText == false)
                child = Text.EMPTY_TEXT;
        } else {
            child = new Text(cs2);
        }

        return child;
    }

    protected Node createCDATANode() {
        String ar = reader.getText();
        String cs = ar;
        Node child = null;

        if (ignoreSpace)
            cs = trimString(ar);

        if (cs.length() == 0) {
            if (ignoreEmptyText == false)
                child = CData.EMPTY_CDATA;
        } else {
            child = new CData(cs);
        }

        return child;
    }

    protected void endElement() throws IOException, XMLStreamException {
        StackItem item = nodeStack.pop();

        if (item.add == true) {
            if (parseEvents != null && current.getNodeType() == Node.ELEMENT_NODE
                    && parseEvents.processNode(nodeStack.size(), (Element) current) == false) {
                item.add = false;
            }

            if (item.add) {
                // Add as a child to the current node.
                //
                item.parent.appendChild(current);
                current.nodeInitialized();
            }
            current = item.parent;
        }
    }

    private void startElement() throws XMLStreamException {

        // Add each attribute.
        //
        int attrCount = reader.getAttributeCount();
        Attribute[] list = new Attribute[attrCount];

        for (int i = 0; i < attrCount; ++i) {
            // Map the namespace string into a shared string.
            //
            String ans = getSharedString(reader.getAttributeNamespace(i));

            // The qname is interned or a shared string.
            //
            String aqname = mapLocalName(reader.getAttributeLocalName(i), true);

            // Value remains unchanged.
            //
            String avalue = getString(reader.getAttributeValue(i));

            // Construct the attribute.
            //
            list[i] = new Attribute(ans, aqname, avalue);
        }

        // Build the parser event source.
        //
        createCtx.attributeList = Attributes.create(list);
        createCtx.namespaceUri = reader.getNamespaceURI() == null ? "" : getSharedString(reader.getNamespaceURI());
        createCtx.prefix = reader.getPrefix() == null ? "" : getSharedString(reader.getPrefix());
        createCtx.name = mapLocalName(reader.getLocalName(), false);

        // Construct the element.
        //
        Element newElem = createElement(createCtx);

        // Check if a new node was created.
        //
        if (newElem == null) {
            // No node was created, clear the bit in the stack for the node.
            // When the closing tag is traversed, we will not set the current
            // node to the parent.
            //
            StackItem stackItem = new StackItem();
            stackItem.add = false;
            stackItem.parent = null;
            nodeStack.add(stackItem);

        } else {
            // Set the pop bit so that the closing tag causes the current node
            // to be reset to the parent.
            //
            StackItem stackItem = new StackItem();
            stackItem.add = true;
            stackItem.parent = current;
            nodeStack.add(stackItem);

            // The new node becomes the context node.
            //
            current = newElem;
        }
    }

    protected String trimString(String a) {

        int start = 0;
        while (start != a.length() && a.charAt(start) <= 32)
            start++;

        int end = a.length();
        while (end > start && a.charAt(end - 1) <= 32)
            end--;

        return a.substring(start, end);
    }

    private String getString(String a) {
        return a;
    }

    public String getSharedString(String a) {
        if (a == null)
            return "";

        if (internNames && a.length() < 100) {
            return a.intern();
        } else {
            return a;
        }
    }

    protected String mapLocalName(String localName, boolean isattr) {
        if (convertLocalNamesToLowerCase)
            localName = localName.toLowerCase();

        return getSharedString(localName);
    }

    public final void setConvertLocalNamesToLowerCase(boolean v) {
        this.convertLocalNamesToLowerCase = true;
    }

    public final void setInternNames(boolean internNames) {
        this.internNames = internNames;
    }

    public final void setIgnoreSpace(boolean ignoreSpace) {
        this.ignoreSpace = ignoreSpace;
    }

    public final void setIgnoreEmptyText(boolean ignoreEmptyText) {
        this.ignoreEmptyText = ignoreEmptyText;
    }

    public void addNodeFactory(NodeFactory factory) {
        if (factory.namespaceUri() == null)
            defaultFactory = factory;
        else
            nodeFactories.put(factory.namespaceUri(), factory);
    }

    public NodeFactory getDefaultNodeFactory() {
        return defaultFactory;
    }

    public void setInputFactory(XMLInputFactory factory) {
        this.inputFactory = factory;
    }

    public void setDomParseEvents(DomParseEvents parserEvents) {
        this.parseEvents = parserEvents;
    }

    public NodeFactory getNodeFactory(CharSequence namespaceURI) {
        NodeFactory lib = nodeFactories.get(namespaceURI);
        return lib;
    }
}
