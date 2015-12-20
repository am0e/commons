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

import java.util.Locale;

/**
 * Interface wraps a MessageSource. This is typically used with
 * {@link MessageSourceResolvable}. Unlike MessageSource, the implementors of
 * this class will hide the locale object so that it does not need to be passed
 * around with the {@link MessageSourceContext} object.
 * <p>
 * The {@link WebRequest} interface implements this interface.
 */
public class MessageSourceContext {
    private MessageSource src;
    private Locale locale;

    public MessageSourceContext(MessageSource src, Locale locale) {
        this.src = src;
        this.locale = locale;
    }

    public String getMessage(String code, Object... args) {
        return src.getMessage(locale, code, args);
    }

    public String getMessageDefault(String code, String defaultMessage, Object... args) {
        return src.getMessageDefault(locale, code, defaultMessage, args);
    }
}