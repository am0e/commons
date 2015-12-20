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

public abstract class SimpleNode implements Node {

    protected Node parent;

    @Override
    public void appendChild(Node child) {
        throw new IllegalAccessError();
    }

    @Override
    public NodeCollection getChildNodes() {
        return NodeCollection.EMPTY_LIST;
    }

    @Override
    public int childNodeSize() {
        return 0;
    }

    @Override
    public Node[] childNodesArray() {
        return null;
    }

    @Override
    public Node getFirstChild() {
        return null;
    }

    @Override
    public Node getLastChild() {
        return null;
    }

    @Override
    public Node getParentNode() {
        return parent;
    }

    @Override
    public String getTextContent() {
        return "";
    }

    @Override
    public void setParentNode(Node parent) {
        this.parent = parent;
    }

    @Override
    public void nodeInitialized() {
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public void setNamespaceURI(String uri) {
    }

    @Override
    public String getNamespaceURI() {
        return null;
    }
}
