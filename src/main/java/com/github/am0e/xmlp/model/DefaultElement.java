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

public class DefaultElement extends AbstractElement {
    /**
     * The name of the element. Eg: "link"
     */
    private String nodeName;

    /**
     * Attributes.
     */
    private Attributes attributes;

    public DefaultElement() {
        this.attributes = Attributes.EMPTY_LIST;
    }

    public DefaultElement(String namespaceUri, String localName, Attributes attributes) {
        this.setNamespaceURI(namespaceUri);
        this.setNodeName(localName);
        this.setAttributes(attributes);
    }

    @Override
    public final String getLocalName() {
        return nodeName;
    }

    protected final void setNodeName(String name) {
        this.nodeName = name;
    }

    protected void setAttributes(Attributes list) {
        attributes = list;
    }

    public final Attributes getAttributes() {
        return attributes;
    }

    public String toString() {
        return nodeName.toString();
    }

    public String getAttributeValue(String attributeName) {
        return attributes.getStringValue(attributeName);
    }

    @Override
    public boolean hasAttribute(String attribute) {
        return attributes.hasAttribute(attribute);
    }
}
