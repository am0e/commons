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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

/**
 * Validation methods for validators.
 */
@Singleton
public class ValidationMethods {
    public static abstract class ValidateMethod {
        /**
         * Validate the value.
         *
         * @param value
         *            The value to validate
         * @return true or null to indicate the value is valid. false to
         *         indicate an invalid value and use the default message code. a
         *         string to indicate the error code.
         */
        public abstract Object validate(Object vaulue);

        public boolean isValid(Object v) {
            return validate(v) == Boolean.TRUE;
        }
    }

    public static class RegExMethod extends ValidateMethod {

        private Pattern p;

        public RegExMethod(String s) {
            p = Pattern.compile(s);
        }

        @Override
        public Object validate(Object value) {
            Matcher m = p.matcher(value.toString());
            return m.matches();
        }

    }

    public final static ValidateMethod ALPHA = new ValidateMethod() {
        public Object validate(Object s) {
            return StringUtils.isAlpha(s.toString()) == true ? Boolean.TRUE : Boolean.FALSE;
        }
    };

    public final static ValidateMethod ALPHA_NUMERIC = new ValidateMethod() {
        public Object validate(Object s) {
            return StringUtils.isAlphanumeric(s.toString());
        }
    };

    public final static ValidateMethod ALPHA_NUMERIC_SPACE = new ValidateMethod() {
        public Object validate(Object s) {
            return StringUtils.isAlphanumericSpace(s.toString());
        }
    };

    public final static ValidateMethod ALPHA_SPACE = new ValidateMethod() {
        public Object validate(Object s) {
            return StringUtils.isAlphaSpace(s.toString());
        }
    };

    public final static ValidateMethod NUMERIC = new ValidateMethod() {
        public Object validate(Object s) {
            return StringUtils.isNumeric(s.toString());
        }
    };

    public final static ValidateMethod NUMERIC_SPACE = new ValidateMethod() {
        public Object validate(Object s) {
            return StringUtils.isNumericSpace(s.toString());
        }
    };

    public final static ValidateMethod URL = new ValidateMethod() {
        public Object validate(Object v) {
            String s = v.toString();

            if (StringUtils.isEmpty(s))
                return false;

            if (s.indexOf("://") != -1)
                return true;

            return false;
        }
    };

    public final static ValidateMethod PHONE_NUMBER = new ValidateMethod() {
        public Object validate(Object v) {
            String s = v.toString();

            return validateChars(s, "0123456789()- +");
        }
    };

    private final Map<String, ValidateMethod> map = new HashMap<>();

    public ValidationMethods() {
        add("Alpha", ALPHA);
        add("AlphaNumeric", ALPHA_NUMERIC);
        add("AlphaNumericSpace", ALPHA_NUMERIC_SPACE);
        add("AlphaSpace", ALPHA_SPACE);
        add("Numeric", NUMERIC);
        add("NumericSpace", NUMERIC_SPACE);
        add("Url", URL);
        add("PhoneNumber", PHONE_NUMBER);
    }

    protected void add(String key, ValidateMethod m) {
        map.put(key, m);
    }

    public ValidateMethod getValidateMethod(String name) {
        if (name.startsWith("~")) {
            return new RegExMethod(name.substring(1));
        } else {
            return map.get(name);
        }
    }

    protected static Object validateChars(String s, String set) {
        return StringUtils.containsOnly(s, set);
    }
}
