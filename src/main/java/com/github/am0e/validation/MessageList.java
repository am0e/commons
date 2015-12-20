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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.am0e.lib.AntLib;
import com.github.am0e.msgs.CodedMessage;

@SuppressWarnings("serial")
public class MessageList implements Serializable {
    /**
     * Collection of field validation error messages.
     */
    private List<FieldError> fieldErrors;

    /**
     * Collection of coded messages, these are not related directly to fields
     * and can be used for information messages as well as error messages,
     * depending on the level.
     */
    private Map<String, List<CodedMessage>> messages;

    private String titleCode;

    public MessageList() {
        fieldErrors = Collections.emptyList();
        messages = Collections.emptyMap();
    }

    public boolean hasErrors() {
        return fieldErrors.isEmpty() == true && getMessages("error").isEmpty() == true ? false : true;
    }

    public int getErrorCount() {
        return fieldErrors.size() + getMessages("error").size();
    }

    public FieldError[] getFieldErrors() {
        return fieldErrors.toArray(new FieldError[0]);
    }

    public FieldError[] getFieldErrors(String fieldName) {
        final int hc = fieldName.hashCode();
        List<FieldError> list = AntLib.newList();
        for (FieldError f : fieldErrors) {
            if (f.isFieldName(fieldName, hc))
                list.add(f);
        }
        return list.toArray(new FieldError[0]);
    }

    public void removeFieldError(String fieldName) {
        final int hc = fieldName.hashCode();
        Iterator<FieldError> iter = fieldErrors.iterator();
        while (iter.hasNext()) {
            FieldError it = iter.next();
            if (it.isFieldName(fieldName, hc))
                iter.remove();
        }
    }

    public void addError(String fieldName, String code, Object value) {
        addError(null, fieldName, fieldName, code, value, null);
    }

    /**
     * @param objectName
     *            Object path. Eg com.arpt.models.catalog.Product
     * @param fieldName
     *            Simple field name. Eg "productName", "lastName", etc.
     * @param propPath
     *            The original parameter path that was used to bind. Eg:
     *            "names[0].lastName".
     * @param code
     *            Constraint error code. For example "email", "required"
     * @param value
     *            Rejected value. null for missing field.
     */
    public void addError(String objectName, String fieldName, String propPath, String code, Object value) {
        addError(objectName, fieldName, propPath, code, value, null);
    }

    /**
     * Adds an error message to the list of errors.
     * 
     * @param objectName
     *            Object path. Eg com.arpt.models.catalog.Product
     * @param fieldName
     *            Simple field name. Eg "productName", "lastName", etc.
     * @param propPath
     *            The original parameter path that was used to bind. Eg:
     *            "names[0].lastName".
     * @param code
     *            Constraint error code. For example "email", "required"
     * @param value
     *            Rejected value. null for missing field.
     * @param arguments
     *            Arguments to use when formatting the error message.
     */
    public void addError(String objectName, String fieldName, String propPath, String code, Object value,
            Object[] arguments) {
        FieldError error = new FieldError(objectName, fieldName, propPath, code, value, arguments);
        addError(error);
    }

    public void addError(FieldError error) {
        if (fieldErrors == Collections.EMPTY_LIST)
            fieldErrors = AntLib.newList(5);

        fieldErrors.add(error);
    }

    public void setTitleCode(String title) {
        this.titleCode = title;
    }

    public String getTitleCode() {
        return titleCode;
    }

    public CodedMessage[] getErrorMessages() {
        return getMessages("error").toArray(new CodedMessage[0]);
    }

    public void addErrorMessage(String code, Object... arguments) {
        addMessage("error", code, arguments);
    }

    public void addSuccessMessage(String code, Object... args) {
        addMessage("success", code, args);
    }

    public void addMessage(String level, String code, Object... arguments) {

        List<CodedMessage> list = messages.get(level);

        if (list == null) {
            if (messages == Collections.EMPTY_MAP)
                messages = AntLib.newHashMap();

            messages.put(level, list = AntLib.newList(5));
        }

        CodedMessage er = new CodedMessage(code, arguments);
        list.add(er);
    }

    public List<CodedMessage> getMessages(String level) {
        List<CodedMessage> list = messages.get(level);
        if (list == null)
            return Collections.emptyList();
        else
            return list;
    }

    public void addFrom(MessageList other) {
        if (other.fieldErrors != null) {
            if (fieldErrors == Collections.EMPTY_LIST)
                fieldErrors = AntLib.newList(5);
            fieldErrors.addAll(other.fieldErrors);
        }

        if (other.messages != null) {
            if (messages == Collections.EMPTY_MAP)
                messages = AntLib.newHashMap();
            this.messages.putAll(other.messages);
        }
    }
}
