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
import java.util.Iterator;

public final class NodeCollection implements Iterable<Node> {

    private final Node[] nodeData;
    private final int size;

    public static final NodeCollection EMPTY_LIST = new NodeCollection(null, 0);
    public static final Node[] EMPTY_ARRAY = new Node[0];

    public NodeCollection(Node[] nodeData, int size) {
        this.nodeData = nodeData;
        this.size = size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Iterator<Node> iterator() {
        return new Iterator<Node>() {
            private int pos = 0;
            final private int isize = size;

            public boolean hasNext() {
                return pos < isize;
            }

            public Node next() {
                return nodeData[pos++];
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public int size() {
        return size;
    }

    public Node get(int pos) {
        assert (pos >= 0 && pos < size);
        return nodeData[pos];
    }

    public Node[] toArray() {
        if (size == 0)
            return EMPTY_ARRAY;
        else
            return Arrays.copyOf(nodeData, size);
    }
}
