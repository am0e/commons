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

/**
 * UUID/ID utility methods.
 * 
 * @author Anthony
 *
 */
public final class Uids {

    /**
     * For converting long to an upper case alpha numeric string. Note no I O U
     * to avoid the creation of offensive words.
     */
    final static char[] cset_base32 = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'F', 'G',
            'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z' };

    public static String getAlphaNumIDFromLong(long v) {
        return encodeLong(v, cset_base32);
    }

    public static long getLongFromAlphaNumID(String id) {
        return decodeLong(id, cset_base32);
    }

    public static String encodeLong(long i, char[] cset) {
        final int radix = cset.length;

        char buf[] = new char[20];
        boolean negative = (i < 0);
        int pos = 19;

        if (!negative) {
            i = -i;
        }

        while (i <= -radix) {
            buf[pos--] = cset[(int) (-(i % radix))];
            i = i / radix;
        }
        buf[pos] = cset[(int) (-i)];

        if (negative) {
            buf[--pos] = '*';
        }

        return new String(buf, pos, (20 - pos));
    }

    public static long decodeLong(String s, char[] cset) throws NumberFormatException {

        if (s == null) {
            throw new IllegalArgumentException();
        }

        if (s.length() == 0) {
            return 0;
        }

        long result = 0;
        int i = 0, max = s.length();
        final int radix = cset.length;
        boolean negative = false;

        if (s.charAt(0) == '*') {
            negative = true;
            i++;
        }

        while (i < max) {
            char c = s.charAt(i++);
            int j;
            for (j = 0; j != radix && cset[j] != c; j++) {
                ;
            }

            if (j == radix) {
                return -1;
            }

            result *= radix;
            result += j;
        }

        return negative ? -result : result;
    }
}
