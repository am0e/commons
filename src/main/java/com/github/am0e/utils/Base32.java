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

//Copyright 2002-2007 Waterken Inc. under the terms of the MIT X license
//found at http://www.opensource.org/licenses/mit-license.html

/**
 * Base32 encoding.
 */
public final class Base32 {

    // Douglas Crockford alphabet:
    // letters I O U are excluded to avoid obscenities and confusion with the
    // number 0.
    // letter L is excluded to avoid confusion with the number 1.
    //
    private final static byte[] dc_alphabet = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z' };

    private Base32() {
    }

    /**
     * Encodes binary data in base32.
     * 
     * @param bytes
     *            binary data
     * @return base32 encoding
     */
    static public String encode(final byte[] bytes) {
        final StringBuilder r = new StringBuilder(bytes.length * 8 / 5 + 1);

        int buffer = 0;
        int bufferSize = 0;
        for (final byte b : bytes) {
            buffer <<= 8;
            buffer |= b & 0xFF;
            bufferSize += 8;
            while (bufferSize >= 5) {
                bufferSize -= 5;
                r.append((char) dc_alphabet[(buffer >>> bufferSize) & 0x1F]);
            }
        }
        if (0 != bufferSize) {
            buffer <<= 5 - bufferSize;
            r.append((char) dc_alphabet[buffer & 0x1F]);
        }

        return r.toString();
    }

    /**
     * Decodes base32 data to binary.
     * 
     * @param chars
     *            base32 data
     * @return decoded binary data
     * @throws InvalidBase32
     *             decoding error
     */
    static public byte[] decode(final String chars) throws UnsupportedEncodingException {
        final int end = chars.length();
        final byte[] r = new byte[end * 5 / 8];
        int buffer = 0;
        int bufferSize = 0;
        int j = 0;
        for (int i = 0; i != end; ++i) {
            final char c = chars.charAt(i);
            buffer <<= 5;
            buffer |= locate(c);
            bufferSize += 5;
            if (bufferSize >= 8) {
                bufferSize -= 8;
                r[j++] = (byte) (buffer >>> bufferSize);
            }
        }
        if (0 != (buffer & ((1 << bufferSize) - 1))) {
            throw new UnsupportedEncodingException();
        }
        return r;
    }

    static private int locate(final char c) throws UnsupportedEncodingException {
        throw new IllegalAccessError();
        // TODO:
        /*
         * return 'a' <= c && 'z' >= c ? c - 'a' : '2' <= c && '7' >= c ? 26 +
         * (c - '2') : 'A' <= c && 'Z' >= c ? c - 'A' : invalid();
         */
    }
}
