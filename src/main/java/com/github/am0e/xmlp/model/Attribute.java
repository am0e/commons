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

import com.github.am0e.msgs.Msgs;

public final class Attribute {
    /**
     * Namespace uri. can be null
     */
    private CharSequence uri;

    /**
     * Local name, no prefix.
     */
    private String qname;

    /**
     * Value
     */
    private Object value;

    public Attribute(CharSequence nsuri, String qname, Object value) {
        this.uri = nsuri;
        this.qname = qname.toString();
        this.value = value;
    }

    public final String getQName() {
        return qname;
    }

    public final String getQname() {
        return qname;
    }

    public final CharSequence getUri() {
        return uri;
    }

    /**
     * Returns the value
     */
    public final Object getValue() {
        return value;
    }

    public final Object setValue(Object value) {
        return this.value = value;
    }

    public final String toString() {
        return Msgs.format("{}=`{}`", qname, value);
    }
}
