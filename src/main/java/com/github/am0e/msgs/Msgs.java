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

import com.github.am0e.providers.ObjectProviderContext;

public final class Msgs {
    private Msgs() {
    }

    /**
     * Simple method to expand a format string using arguments. Unlike the Java
     * MessageFormat variation, this method will replace use toString() on the
     * argument to get the string value used to replace the {n} sequences in the
     * format string. {} is also supported which uses an internal index starting
     * from 0. For example:
     * 
     * <pre>
     * format("Name is {0} age is {1}", "John", 49);
     * format("Name is {} age is {}", "John", 49);
     * </pre>
     * 
     * @param format
     * @param args
     * @return
     */
    public final static String format(String format, Object... args) {
        StringBuilder sb = new StringBuilder();
        format(sb, format, args);
        return sb.toString();
    }

    public final static String format(String format, Object arg) {
        StringBuilder sb = new StringBuilder();
        format(sb, format, new Object[] { arg });
        return sb.toString();
    }

    public final static void format(StringBuilder sb, String format, Object... args) {

        if (format.charAt(0) == '@') {
            format = Msgs.source(Locale.getDefault()).getMessage(format.substring(1));
        }

        final int sz = format.length();
        int i;
        int last = 0;
        int index = -1;

        // Look for '{'
        //
        while (last < sz && (i = format.indexOf('{', last)) != -1) {

            // Append last chunk.
            //
            if (last != i) {
                sb.append(format, last, i);
            }

            i++;
            int start = i;

            // Convert index to an integer.
            // Note the small optimisation here to inline conversion of a single
            // digit which is 99%
            // of all cases.
            //
            if (format.charAt(i) == '}') {
                // no digit {}
                //
                index++;
                last = i + 1;

            } else if (format.charAt(i + 1) == '}') {
                // Single digit {0}
                //
                index = format.charAt(start) - '0';
                last = i + 2;

            } else {
                // Get index.
                //
                while ((format.charAt(i) != ',' && format.charAt(i) != '}')) {
                    i++;
                }

                // Multiple digits {12}
                //
                index = Integer.parseInt(format.substring(start, i));
                last = i + 1;
            }

            // Validate arg index and get arg.
            //
            String s;
            if (index < 0 || index >= args.length)
                s = "ndx-error";
            else
                s = args[index] == null ? null : args[index].toString();

            // Append arg.
            //
            sb.append(s == null ? "null" : s);
        }

        // Append last chunk.
        //
        if (last < sz) {
            sb.append(format, last, sz);
        }
    }

    public static MessageSourceContext source() {
        return Msgs.source("strings");
    }

    public static MessageSourceContext source(Locale locale) {
        return Msgs.source("strings", locale);
    }

    public static MessageSourceContext source(String bundle) {
        return ObjectProviderContext.instanceOf(MessageSourceContextProvider.class).getMessageSourceContext(bundle);
    }

    public static MessageSourceContext source(String bundle, Locale locale) {
        return ObjectProviderContext.instanceOf(MessageSourceContextProvider.class).getMessageSourceContext(bundle, locale);
    }
}
