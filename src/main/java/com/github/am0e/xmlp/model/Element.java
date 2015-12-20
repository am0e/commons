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

public interface Element extends Node {
    public Attributes getAttributes();

    public boolean hasAttribute(String attribute);

    /**
     * Get all child elements with the specified local name.
     */
    public Element[] getElements(CharSequence localName);

    public Element[] getChildElements();

    public Element getElement(CharSequence localName);

    public Element findElement(String nodePath);

    /**
     * Returns the text content of the child node. An empty string is returned
     * if the child does not exist.
     * 
     * @param childLocalName
     * @return
     */
    public String getChildText(String childLocalName);

    /**
     * Retrieves an attribute value by name.
     * 
     * @param attributeName
     *            The name of the attribute to retrieve.
     * @return The <code>Attr</code> value as a string, or the empty string if
     *         that attribute does not have a specified or default value.
     */
    public String getAttributeValue(String attributeName);

    public Node getRootParent();
}
