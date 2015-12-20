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

public class CData extends SimpleNode {

    public static final CData EMPTY_CDATA = new CData("");

    private String content;

    public CData(String str) {
        content = str;
    }

    @Override
    public final String toString() {
        return "CData:" + content;
    }

    @Override
    public String getTextContent() {
        return content;
    }

    @Override
    public short getNodeType() {
        return CDATA_NODE;
    }
}
