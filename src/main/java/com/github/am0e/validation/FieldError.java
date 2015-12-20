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
package com.github.am0e.validation;

import java.io.Serializable;

import com.github.am0e.msgs.MessageSourceContext;
import com.github.am0e.msgs.MessageSourceResolvable;

@SuppressWarnings("serial")
public final class FieldError implements Serializable, MessageSourceResolvable {
    /**
     * Object path. Eg com.arpt.models.catalog.Product
     */
    final String objectName;

    /**
     * Simple field name. Eg "productName", "lastName", etc.
     */
    final String fieldName;

    /**
     * Hashcode for {@link #fieldName}
     */
    final int fieldNameHC;

    /**
     * The original parameter path that was used to bind. Eg:
     * "names[0].lastName". This is here to allow UI tags to associate a
     * FieldError with an HTML Input tag.
     */
    final String propPath;

    /**
     * Hashcode for {@link #propPath}
     */
    final int propPathHC;

    /**
     * Constraint error code. For example "email", "required"
     */
    final String code;

    /**
     * Rejected value. null for missing field.
     */
    final Object value;

    /**
     * Arguments to use when formatting the error message.
     */
    final Object[] arguments;

    public FieldError(String objectName, String fieldName, String propPath, String code, Object value,
            Object[] arguments) {
        this.objectName = objectName;
        this.fieldName = fieldName;
        this.fieldNameHC = fieldName.hashCode();
        this.propPath = propPath;
        this.propPathHC = propPath == null ? 0 : propPath.hashCode();
        this.code = code;
        this.value = value;
        this.arguments = arguments;
    }

    public final String getObjectName() {
        return objectName;
    }

    public final String getFieldName() {
        return fieldName;
    }

    public final String getPropPath() {
        return propPath;
    }

    public final String getCode() {
        return code;
    }

    public final Object getValue() {
        return value;
    }

    public final Object[] getArguments() {
        return arguments;
    }

    public String getDisplayFieldName(MessageSourceContext source) {
        StringBuilder sb = new StringBuilder();
        sb.append("field");
        sb.append('.');
        int len0 = sb.length();
        String msg = null;

        if (objectName != null) {
            sb.append(objectName);
            sb.append('.');
            sb.append(fieldName);

            // field.{objectname}.{fieldname}
            // field.{fieldname}
            //
            msg = source.getMessageDefault(sb.toString(), null);
        }

        if (msg == null) {
            sb.setLength(len0);
            sb.append(fieldName);

            // field.{fieldname}
            //
            msg = source.getMessageDefault(sb.toString(), fieldName);
        }

        return msg;
    }

    public String getMessage(MessageSourceContext source) {
        return getMessage(source, arguments);
    }

    public String getMessage(MessageSourceContext source, Object[] arguments) {
        StringBuilder sb = new StringBuilder();
        sb.append("code");
        sb.append('.');
        int len0 = sb.length();
        sb.append(objectName);
        int len1 = sb.length();
        sb.append('.');
        sb.append(fieldName);
        int len2 = sb.length();
        sb.append('.');
        sb.append(code);

        String displayFieldName = getDisplayFieldName(source);
        Object args[];

        // Make space for the first 3 parameters - { "fieldname", "value" }
        //
        if (arguments == null) {
            args = new Object[2];
        } else {
            args = new Object[arguments.length + 2];
            System.arraycopy(arguments, 0, args, 2, arguments.length);
        }

        args[0] = displayFieldName;
        args[1] = value;

        // objectname.fieldname.code
        //
        String msg = source.getMessageDefault(sb.toString(), null, args);
        if (msg == null) {
            sb.setLength(len2);

            // objectname.fieldname
            //
            msg = source.getMessageDefault(sb.toString(), null, args);
        }

        if (msg == null) {
            // objectname
            //
            sb.setLength(len1);
            msg = source.getMessageDefault(sb.toString(), null, args);
        }

        if (msg == null) {
            // code.{fieldname}.{code}
            //
            sb.setLength(len0);
            sb.append(fieldName);
            sb.append('.');
            sb.append(code);
            msg = source.getMessageDefault(sb.toString(), null, args);
        }

        if (msg == null) {
            // code.{fieldname}
            //
            sb.setLength(len0);
            sb.append(fieldName);
            msg = source.getMessageDefault(sb.toString(), null, args);
        }

        if (msg == null) {
            // code.{code}
            //
            sb.setLength(len0);
            sb.append(code);
            msg = source.getMessageDefault(sb.toString(), code, args);
        }

        return msg;
    }

    public boolean isFieldName(String fieldName, int hashCode) {
        return this.fieldNameHC == hashCode && this.fieldName.equals(fieldName);
    }
}
