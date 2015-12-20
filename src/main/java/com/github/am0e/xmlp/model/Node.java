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

public interface Node {
    /**
     * Returns the local name. This returns null if the node is not an Element.
     */
    public String getLocalName();

    /**
     * Get the parent node.
     */
    public Node getParentNode();

    /**
     * The first child of this node. If there is no such node, this returns
     * <code>null</code>.
     */
    public Node getFirstChild();

    /**
     * The last child of this node. If there is no such node, this returns
     * <code>null</code>.
     */
    public Node getLastChild();

    public NodeCollection getChildNodes();

    /**
     * Returns the text content of this node. If the node has no text, an empty
     * string ("") is returned.
     */
    public String getTextContent();

    /**
     * Gets the count of child nodes.
     * 
     * @return
     */
    public int childNodeSize();

    /**
     * Get the underlying array holding the child collection. If iterating over
     * the array use childNodeSize() rather than the lengh of the array as the
     * array may contain unused entries at the end of the array. If there are no
     * child nodes. the return value will be null.
     * 
     * @return
     */
    public Node[] childNodesArray();

    public void appendChild(Node child);

    public void setParentNode(Node parent);

    public void nodeInitialized();

    public void setNamespaceURI(String uri);

    public String getNamespaceURI();

    public short getNodeType();

    /**
     * The node is an <code>Element</code>.
     * 
     * @see Element
     */
    public static final short ELEMENT_NODE = 1;

    /**
     * The node is a <code>Text</code> node.
     * 
     * @see Node
     */
    public static final short TEXT_NODE = 3;

    /**
     * The node is a <code>Document</code>.
     * 
     * @see Document
     */
    public static final short DOCUMENT_NODE = 9;

    /**
     * The node is a <code>CDATASection</code>.
     * 
     * @see CData
     */
    public static final short CDATA_NODE = 4;
}
