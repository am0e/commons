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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceBundleMessageSource implements MessageSource {

    private String baseName;
    private ClassLoader loader;
    private ResourceBundleMessageSource parent;

    public ResourceBundleMessageSource() {
        loader = Thread.currentThread().getContextClassLoader();
    }

    public void setParent(ResourceBundleMessageSource parent) {
        this.parent = parent;
    }

    public String getMessage(Locale locale, String code, Object... args) {
        if (locale == null)
            locale = Locale.getDefault();

        return getResourceString(locale, code, args);
    }

    public String getMessageDefault(Locale locale, String code, String defaultMessage, Object... args) {

        if (locale == null)
            locale = Locale.getDefault();
        String s = null;

        s = getResourceString(locale, code, args);

        if (s == null && defaultMessage != null) {
            s = args == null ? defaultMessage : MessageFormat.format(defaultMessage, args);
        }

        return s;
    }

    protected String getResourceString(Locale locale, String code, Object[] args) {

        ResourceBundle rb = ResourceBundle.getBundle(baseName, locale, loader);

        if (rb.containsKey(code)) {
            String fmt = rb.getString(code);
            return (args == null || fmt == null) ? fmt : MessageFormat.format(fmt, args);

        } else if (parent != null) {
            return parent.getResourceString(locale, code, args);
        }

        return null;
    }

    public final String getBaseName() {
        return baseName;
    }

    public final void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public final ClassLoader getBundleClassLoader() {
        return loader;
    }

    public final void setBundleClassLoader(ClassLoader loader) {
        this.loader = loader;
    }

    public final void setBundleLoader(String type) {
        if (type.equals("threadContextClassLoader"))
            this.loader = Thread.currentThread().getContextClassLoader();

        else if (type.equals("classLoader"))
            this.loader = this.getClass().getClassLoader();
    }
}
