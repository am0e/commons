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

import java.io.IOException;

import com.github.am0e.utils.StringBuilderWriter;

public class Document extends AbstractNode {
    public Element getDocumentElement() {
        return (Element) getFirstChild();
    }

    public NodeWalker getNodeWalker() {
        return new NodeWalker(getFirstChild());
    }

    public Document getDocumentFragment(Element child) {
        Document doc = new Document();
        doc.appendChild(child);
        return doc;
    }

    public String toXmlString() throws IOException {
        StringBuilderWriter sb = new StringBuilderWriter();
        toXml(sb, this.getFirstChild());
        return sb.toString();
    }

    public static void toXml(Appendable sb, Node e) throws IOException {
        if (e instanceof Element) {
            sb.append("<");
            sb.append(e.getLocalName());
            Element el = (Element) e;
            Attributes al = el.getAttributes();
            int sz = al.size();
            for (int i = 0; i != sz; i++) {
                sb.append(' ');
                sb.append(al.get(i).getQName());
                sb.append('=');
                sb.append('\"');
                sb.append(al.get(i).getValue().toString());
                sb.append('\"');
            }
            sb.append(">");
            NodeCollection nc = e.getChildNodes();
            for (int i = 0; i != nc.size(); i++) {
                toXml(sb, nc.get(i));
            }
            sb.append("</");
            sb.append(e.getLocalName());
            sb.append(">");
        } else {
            sb.append(e.getTextContent());
        }
    }

    @Override
    public short getNodeType() {
        return DOCUMENT_NODE;
    }
}
