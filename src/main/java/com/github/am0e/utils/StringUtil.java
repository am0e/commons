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

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.StringTokenizer;

import com.github.am0e.lib.AntLib;

/**
 * Misc String Utility Functions
 * 
 */
public final class StringUtil {

    /**
     * Splits a String on a delimiter into a List of Strings.
     * 
     * @param str
     *            the String to split
     * @param delim
     *            the delimiter character(s) to split on (null will split on
     *            whitespaces)
     * @return a list of Strings
     */
    public static String[] split(String str, String delim) {
        List<String> list = AntLib.newList();

        if (str != null) {
            StringTokenizer st;

            if (delim == null)
                st = new StringTokenizer(str);
            else
                st = new StringTokenizer(str, delim);

            while (st.hasMoreTokens())
                list.add(st.nextToken().trim());
        }

        return list.toArray(new String[0]);
    }

    public static byte[] encodeUTF8(String s) {
        try {
            return s.getBytes("UTF-8");

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String decodeUTF8(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Optimised version of {@link String#getBytes()}. Useful when you know the
     * input string is a simple ascii string containing no chars above ascii
     * code 255. Eg for getting the byte array of a base64 encoded string.
     * 
     * @param s
     *            The input string.
     * @return
     */
    @SuppressWarnings("deprecation")
    public static byte[] getAsciiBytes(String s) {
        byte[] bytes = new byte[s.length()];
        s.getBytes(0, s.length(), bytes, 0);
        return bytes;
    }

    /**
     * Simple and fast string compare. String s1 can have '?' which means
     * compare any character Eg: "PO3?" will match "PO31", "PO32", etc.
     * 
     * @param s1
     * @param s2
     * @return
     */
    public static boolean wildStartsWith(String s1, String s2) {
        final int n1 = s1.length(), n2 = s2.length();
        if (n2 < n1)
            return false;
        else {
            int i1, i2;
            for (i1 = 0, i2 = 0; i1 < n1 && i2 < n2; i1++, i2++) {
                char c1 = s1.charAt(i1);
                if (c1 != s2.charAt(i2) && c1 != '?')
                    return false;
            }

            return i1 == i2;
        }
    }

    public static boolean wildcardMatch(String str, String pattern, boolean ignoreCase) {
        if (ignoreCase)
            return wildcardMatch(str.toLowerCase(), pattern.toLowerCase());
        else
            return wildcardMatch(str, pattern);
    }

    public static boolean wildcardMatch(String str, String pattern) {
        if (pattern == null || pattern.isEmpty())
            return str.length() == 0;

        final int lS = str.length();
        final int lW = pattern.length();
        int pS = 0;
        int pW = 0;

        if (pattern.length() == 1) {
            if (pattern.charAt(0) == '*')
                return true;
        }

        while (pS < lS && pW < lW && pattern.charAt(pW) != '*') {
            final char wild = pattern.charAt(pW);
            if (wild != '?' && wild != str.charAt(pS))
                return false;
            pW++;
            pS++;
        }

        int pSm = 0;
        int pWm = 0;
        while (pS < lS && pW < lW) {
            char wild = pattern.charAt(pW);
            if (wild == '*') {
                pW++;
                if (pW == lW)
                    return true;
                pWm = pW;
                pSm = pS + 1;
            } else if (wild == '?' || wild == str.charAt(pS)) {
                pW++;
                pS++;
            } else {
                pW = pWm;
                pS = pSm;
                pSm++;
            }
        }
        while (pW < lW && pattern.charAt(pW) == '*')
            pW++;

        return (pW == lW && pS == lS);
    }

    public static String abbreviateAtWord(String str, int maxlen, String elipses) {
        if (str.length() > maxlen) {
            maxlen -= elipses.length();
            int pos = str.lastIndexOf(' ', maxlen);
            if (pos == -1 || pos > maxlen) {
                str = "";
            } else {
                str = str.substring(0, pos);
                if (elipses.isEmpty() == false) {
                    str += elipses;
                }
            }
        }
        return str;
    }

    public static String squeezeSpaces(String str) {
        char[] chars = str.toCharArray();
        StringBuilder sb = new StringBuilder(chars.length);

        boolean spc = false;
        for (int i = 0; i != chars.length; i++) {
            char ch = chars[i];
            if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
                spc = true;
            } else {
                if (spc && sb.length() != 0) {
                    sb.append(' ');
                    spc = false;
                }
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public final static String removeChars(String s, String charSet) {
        if (s == null || s.isEmpty())
            return s;

        char[] chars = s.toCharArray();
        StringBuilder sb = new StringBuilder(chars.length);

        for (int i = 0; i != chars.length; i++) {
            if (charSet.indexOf(chars[i]) == -1)
                sb.append(chars[i]);
        }

        return sb.toString();
    }
}
