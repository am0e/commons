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
import java.util.Collections;
import java.util.List;

public abstract class AbstractNode implements Node {
    /**
     * The parent of this node.
     */
    protected Node parentNode;

    /**
     * Namespace URL
     */
    private String namespaceURI;

    /**
     * The children of this node.
     */
    protected Node[] nodeData;
    protected short size;

    public final static List<Element> EMPTY_LIST = Collections.emptyList();

    @Override
    public void appendChild(Node child) {
        // Set the parent of the child to this.
        //
        child.setParentNode(this);

        ensureCapacity(size + 1);
        nodeData[size++] = child;
    }

    @Override
    public final Node getParentNode() {
        return parentNode;
    }

    @Override
    public final void setParentNode(Node parent) {
        this.parentNode = parent;
    }

    @Override
    public void nodeInitialized() {
        if (nodeData != null) {
            if (size < nodeData.length)
                nodeData = Arrays.copyOf(nodeData, size);
        }
    }

    @Override
    public int childNodeSize() {
        return size;
    }

    @Override
    public Node[] childNodesArray() {
        return nodeData;
    }

    @Override
    public Node getFirstChild() {
        return nodeData == null ? null : nodeData[0];
    }

    @Override
    public Node getLastChild() {
        if (size == 0)
            return null;
        else
            return nodeData[size - 1];
    }

    @Override
    public NodeCollection getChildNodes() {
        return nodeData == null ? NodeCollection.EMPTY_LIST : new NodeCollection(nodeData, size);
    }

    @Override
    public final String getTextContent() {
        if (nodeData != null) {
            Node child = nodeData[0];
            if (child instanceof Text)
                return ((Text) child).getTextContent();
        }
        return "";
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public final void setNamespaceURI(String uri) {
        this.namespaceURI = uri;
    }

    @Override
    public String getNamespaceURI() {
        return this.namespaceURI;
    }

    void ensureCapacity(int minCapacity) {
        if (nodeData == null) {
            nodeData = new Node[Math.max(minCapacity, 1)];
        } else {
            int oldCapacity = nodeData.length;
            if (minCapacity > oldCapacity) {
                Object oldData[] = nodeData;
                int newCapacity = (oldCapacity * 3) / 2 + 1;
                if (newCapacity < minCapacity)
                    newCapacity = minCapacity;
                nodeData = new Node[newCapacity];
                System.arraycopy(oldData, 0, nodeData, 0, size);
            }
        }
    }
}
