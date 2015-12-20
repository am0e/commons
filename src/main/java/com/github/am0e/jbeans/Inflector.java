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
package com.github.am0e.jbeans;

import com.github.am0e.utils.CharNormalizer;

public final class Inflector {

    public static String mapToJava(String name, char delimChar, boolean ucFirst) {

        char[] chars = name.toLowerCase().trim().toCharArray();
        final int last = chars.length - 1;
        int pos = 0;

        for (int i = 0; i <= last; i++) {
            if (chars[i] == delimChar && i != last)
                chars[pos++] = Character.toUpperCase(chars[++i]);
            else
                chars[pos++] = chars[i];
        }

        if (ucFirst && pos != 0)
            chars[0] = Character.toUpperCase(chars[0]);

        return String.valueOf(chars, 0, pos);
    }

    public static String fieldFromMethodName(String methodName, int off) {
        char[] chars = methodName.toCharArray();

        // Allow for getUI as UI, getUID as UID, isUI as UI, etc.
        //
        if (chars.length <= off + 1 || Character.isLowerCase(chars[off + 1])) {
            chars[off] = Character.toLowerCase(chars[off]);
        }
        return String.valueOf(chars, off, chars.length - off);
    }

    public static String getGetterMethod(String fieldName) {
        char[] chars = new char[fieldName.length() + 3];
        chars[0] = 'g';
        chars[1] = 'e';
        chars[2] = 't';
        fieldName.getChars(0, fieldName.length(), chars, 3);
        chars[3] = Character.toUpperCase(chars[3]);
        return String.valueOf(chars);
    }

    public static String getIsMethod(String fieldName) {
        char[] chars = new char[fieldName.length() + 2];
        chars[0] = 'i';
        chars[1] = 's';
        fieldName.getChars(0, fieldName.length(), chars, 2);
        chars[2] = Character.toUpperCase(chars[2]);
        return new String(chars);
    }

    public static String getSetterMethod(String fieldName) {
        char[] chars = new char[fieldName.length() + 3];
        chars[0] = 's';
        chars[1] = 'e';
        chars[2] = 't';
        fieldName.getChars(0, fieldName.length(), chars, 3);
        chars[3] = Character.toUpperCase(chars[3]);
        return new String(chars);
    }

    /**
     * Maps a java identifier "EntityTask" to a lowercase db name "entity_task"
     * 
     * @param dataSourceName
     * @return
     */
    public static String mapJavaNameToDBName(String javaName) {

        if (javaName == null)
            return null;
        if (javaName.length() <= 0)
            return "";

        char[] chars = javaName.toCharArray();
        char[] dest = new char[javaName.length() * 2];
        int dpos = 1;

        dest[0] = Character.toLowerCase(chars[0]);

        for (int i = 1; i < chars.length; i++) {

            if (Character.isUpperCase(chars[i]))
                dest[dpos++] = '_';

            dest[dpos++] = Character.toLowerCase(chars[i]);
        }

        return String.valueOf(dest, 0, dpos);
    }

    /**
     * Maps a java identifier to a readable name. Eg "visibleItemCount" maps to
     * "Visible Item Count", "ETLTask" maps to "ETL Task",
     * 
     * @param javaName
     * @return
     */
    public static String mapJavaNameToPublicName(String javaName) {

        if (javaName == null)
            return null;
        if (javaName.length() <= 1)
            return javaName;

        char[] chars = javaName.toCharArray();

        int dpos = 1;
        int i = 1;
        char[] dest = new char[chars.length * 2];
        dest[0] = Character.toUpperCase(chars[0]);

        for (; i < chars.length - 1; i++) {
            if (Character.isUpperCase(chars[i]) && Character.isLowerCase(chars[i + 1]))
                dest[dpos++] = ' ';

            dest[dpos++] = chars[i];
        }

        dest[dpos++] = chars[i];

        return String.valueOf(dest, 0, dpos);
    }

    public static String lowerCaseFirst(String name) {
        char[] chars = new char[name.length()];
        name.getChars(0, chars.length, chars, 0);
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    public static String upperCaseFirst(String name) {
        char[] chars = new char[name.length()];
        name.getChars(0, chars.length, chars, 0);
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    public static String convertNameToId(String name, char sepChar) {
        char[] seq = name.toCharArray();
        int pos = 0;

        for (int i = 0; i != seq.length; i++) {
            char c = seq[i];
            if (c > 127) {
                c = CharNormalizer.normalize(c);
                if (c < 127)
                    seq[pos++] = c;
            } else if (c == ' ') {
                seq[pos++] = sepChar;
            } else if (Character.isLetterOrDigit(c)) {
                seq[pos++] = Character.toLowerCase(c);
            }
        }
        return new String(seq, 0, pos);
    }
}
