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

import com.github.am0e.utils.MultiValueMap;
import com.github.am0e.validation.ValidationMethods.ValidateMethod;

/**
 * Validator for programatic validation of query parameters, etc.
 * 
 * @author Anthony
 */
public final class ProgramaticValidator {
    private final MultiValueMap params;
    private final ValidationMethods validationMethods;
    private final MessageList msgs;

    /**
     * Constructor.
     * 
     * @param msgs
     *            Field errors target.
     * @param params
     *            Query parameters to validate.
     * @param validationMethods
     */
    public ProgramaticValidator(ValidationMethods methods, MessageList msgs, MultiValueMap params) {
        this.msgs = msgs;
        this.validationMethods = methods;
        this.params = params;
    }

    private Object getValue(String param, boolean required) {
        Object v = params.getObject(param, null);

        if (v != null) {
            // In multipart forms, v may be a DiskFileItem object!
            // an empty string is treated as no value and we will return null.
            //
            if (!v.toString().isEmpty())
                return v;
        }

        if (required)
            addError(param, "required", null);

        return null;
    }

    /**
     * Validate a required parameter.
     * 
     * @param param
     * @return
     */
    public ProgramaticValidator required(String param) {
        getValue(param, true);
        return this;
    }

    /**
     * Validate an expresssion.
     * 
     * @param param
     *            The name of the field to validate.
     * @param exprResult
     *            Result of expression. If this is false, the validation will
     *            fail.
     * @return
     */
    public ProgramaticValidator validateExpr(String param, boolean exprResult) {
        if (!exprResult) {
            addError(param, "invalid", null);
        }
        return this;
    }

    public ProgramaticValidator required(String param, int max) {
        return validate(param, 1, max);
    }

    /**
     * Validate the field length. If the field is null or an empty string, the
     * error code will be "required". If the field is not within the range, the
     * error code will be "length".
     * 
     * @param param
     * @param min
     * @param max
     * @return
     */
    public ProgramaticValidator validate(String param, int min, int max) {
        Object v = getValue(param, (min >= 1));
        if (v != null) {
            String s = v.toString();
            if (s.length() < min || s.length() > max) {
                addError(param, "length", null);
            }
        }
        return this;
    }

    public ProgramaticValidator validate(String param, boolean required, String validationMethod) {
        Object v = getValue(param, required);
        if (v != null) {
            validateUsingMethod(param, v, validationMethods.getValidateMethod(validationMethod));
        }
        return this;
    }

    public ProgramaticValidator validate(String param, boolean required, ValidateMethod method) {
        Object v = getValue(param, required);
        if (v != null) {
            validateUsingMethod(param, v, method);
        }
        return this;
    }

    private void validateUsingMethod(String param, Object v, ValidateMethod method) {
        Object vr = (method == null ? null : method.validate(v));

        if (vr == Boolean.FALSE) {
            // Failed validation.
            addError(param, "invalid", null);

        } else if (vr != Boolean.TRUE && vr != null) {
            // Convert into a message code.
            addError(param, vr.toString(), null);
        }
    }

    public ProgramaticValidator match(String param, String regex) {
        Object v = getValue(param, true);
        if (v != null) {
            String s = v.toString();
            if (s.matches(regex) == false) {
                addError(param, "invalid", null);
            }
        }
        return this;
    }

    public void addError(String param, String code, Object value) {
        msgs.addError(param, code, null);
    }

    public MessageList errors() {
        return msgs;
    }

    public boolean hasErrors() {
        return msgs.hasErrors();
    }

    public void validate() {
        if (hasErrors())
            fail();
    }

    public void fail() {
        throw new ValidationException();
    }
}
