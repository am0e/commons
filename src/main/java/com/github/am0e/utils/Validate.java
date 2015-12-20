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
package com.github.am0e.utils;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.am0e.msgs.Msgs;

final public class Validate {
    public final static Logger log = LoggerFactory.getLogger(Validate.class);

    public static IllegalArgumentException illegalArgument(String paramName) {
        return new IllegalArgumentException(Msgs.format("Illegal Argument: `{}`", paramName));
    }

    public static IllegalArgumentException illegalArgument(String paramName, String msg, Object... msgArgs) {
        return new IllegalArgumentException(
                Msgs.format("Illegal Argument: `{}`, {}", paramName, Msgs.format(msg, msgArgs)));
    }

    public static void paramNotNull(Object object, String msg, Object... args) {
        if (object == null)
            throw new IllegalArgumentException(Msgs.format(msg, args));
    }

    public static void paramNotNull(Object object) {
        if (object == null)
            throw new IllegalArgumentException("notNull failed");
    }

    public static void paramIsTrue(boolean expr, String msg, Object... args) {
        if (expr == false)
            throw new IllegalArgumentException(Msgs.format(msg, args));
    }

    public static void paramIsTrue(boolean expr) {
        if (expr == false)
            throw new IllegalArgumentException();
    }

    public static void paramIsFalse(boolean expr) {
        if (expr == true)
            throw new IllegalArgumentException();
    }

    public static void paramNotEmpty(Object[] array, String msg, Object... args) {
        if (array == null || array.length == 0)
            throw new IllegalArgumentException(Msgs.format(msg, args));
    }

    public static void paramNotEmpty(Collection<?> c, String msg, Object... args) {
        if (c == null || c.isEmpty())
            throw new IllegalArgumentException(Msgs.format(msg, args));
    }

    public static void stateIsTrue(boolean expr, String msg, Object... args) {
        if (expr == false)
            throw new IllegalStateException(Msgs.format(msg, args));
    }

    public static void stateIsTrue(boolean expr) {
        if (expr == false)
            throw new IllegalStateException();
    }

    public static void stateIsNull(Object v) {
        if (v != null)
            throw new IllegalStateException();
    }

    public static void logError() {
        logError(null);
    }

    // sts[0] = java.lang.Thread :: getStackTrace
    // sts[1] = com.arpt.core.Validate :: error ( This function )
    // sts[2] = Where you called the error function
    // sts[3] = ....
    //
    public static void logError(String msg) {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();

        if (sts != null && sts.length > 2) {
            StackTraceElement ste = sts[2];
            log.error(String.format("Error at (%s#%s:%d) %s", ste.getClassName(), ste.getMethodName(),
                    ste.getLineNumber(), msg == null ? "" : msg));
        }
    }

    public static AccessNotAllowedException notImplementedException() {
        return new AccessNotAllowedException("Method Not Implemented");
    }

    public static AccessNotAllowedException notAllowedException(String msg) {
        return new AccessNotAllowedException(msg);
    }

    public static UnsupportedException unsupportedException() {
        return new UnsupportedException();
    }

    public static NullPointerException nullPointerException(String msg) {
        return new NullPointerException(msg);
    }
}
