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
package com.github.am0e.msgs;

import java.io.Serializable;

@SuppressWarnings("serial")
public final class CodedMessage implements MessageSourceResolvable, Serializable {
    /**
     * Message code.
     */
    final String code;

    /**
     * Arguments to use when formatting the message.
     */
    final Object[] arguments;

    public CodedMessage(String code, Object[] arguments) {
        this.code = code;
        this.arguments = arguments;
    }

    public final String getCode() {
        return code;
    }

    public final Object[] getArguments() {
        return arguments;
    }

    public String getMessage(MessageSourceContext source) {
        return getMessage(source, arguments);
    }

    public String getMessage(MessageSourceContext source, Object[] arguments) {
        return source.getMessageDefault(code, code, arguments);
    }
}
