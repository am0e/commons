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
package com.github.am0e.xmlp.model;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractElement extends AbstractNode implements Element {

    public static final Element[] EMPTY_ARRAY = new Element[0];

    @Override
    public short getNodeType() {
        return ELEMENT_NODE;
    }

    @Override
    public Element[] getElements(CharSequence localName) {
        if (nodeData == null) {
            return EMPTY_ARRAY;
        } else {
            Element list[] = new Element[size];
            int pos = 0;

            for (int i = 0; i < size; i++) {
                Node node = nodeData[i];
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (localName.equals(node.getLocalName())) {
                        list[pos++] = (Element) node;
                    }
                }
            }
            return Arrays.copyOf(list, pos);
        }
    }

    @Override
    public Element[] getChildElements() {
        if (nodeData == null) {
            return EMPTY_ARRAY;
        } else {
            Element list[] = new Element[size];
            int pos = 0;

            for (int i = 0; i < size; i++) {
                Node node = nodeData[i];
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    list[pos++] = (Element) node;
                }
            }

            return Arrays.copyOf(list, pos);
        }
    }

    @Override
    public Element findElement(String nodePath) {
        String[] list = StringUtils.split(nodePath, '/');
        Element child = null;
        Element node = this;

        for (int i = 0; i != list.length && node != null; i++) {
            child = node.getElement(list[i]);
            node = child;
        }

        return child;
    }

    @Override
    public String getChildText(String childLocalName) {
        Element child = getElement(childLocalName);
        if (child != null) {
            return child.getTextContent();
        } else
            return "";
    }

    @Override
    public Element getElement(CharSequence localName) {
        if (nodeData != null) {
            for (int i = 0; i < size; i++) {
                Node node = nodeData[i];
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (localName.equals(node.getLocalName())) {
                        return (Element) node;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public final Node getRootParent() {
        Node it = this;
        for (;;) {
            Node parent = it.getParentNode();
            if (parent == null) {
                return it;
            }
            it = parent;
        }
    }

}
