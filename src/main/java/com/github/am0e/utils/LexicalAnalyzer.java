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
 * Simple generic lexical parser.
 * 
 * @author Anthony (ARPT)
 */
public class LexicalAnalyzer {

    protected char[] source;
    protected int sourcePos;
    protected String val;
    protected StringBuilder sb;
    protected boolean parseEscapes;
    private int tok;
    public static final int EOF = -1;
    public static final int IDENTIFIER = 2;
    public static final int STRING = 3;
    public static final int NUMBER = 4;
    public static final int SYMBOL = 6;

    public LexicalAnalyzer() {
        sb = new StringBuilder();
    }

    public LexicalAnalyzer(String source) {
        this();
        setSource(source);
    }

    public void setSource(String source) {
        setSource(source.toCharArray());
    }

    public void setSource(char[] source) {
        this.source = source;
        this.sourcePos = 0;
        this.val = null;
        this.tok = -1;
    }

    /**
     * Return true if char is whitespace.
     */
    protected boolean isSpace(char c) {
        return (c == ' ' || c == '\t' || c == '\r' || c == '\n');
    }

    protected boolean isIdentifierStart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    protected boolean isIdentifier(char c) {
        return isIdentifierStart(c) || (c >= '0' && c <= '9');
    }

    protected boolean isString(char c) {
        return (c == '\'' || c == '\"');
    }

    protected boolean isNumber(char c) {
        return (c >= '0' && c <= '9');
    }

    protected boolean isSymbol(char c) {
        return "!$%^&*()-=+[]{};:@#~<,>./?|\\".indexOf(c) != -1;
    }

    protected boolean isParen(char c) {
        return "()[]{}".indexOf(c) != -1;
    }

    protected boolean istermSymbol(char c) {
        return "()[]{},.`".indexOf(c) != -1;
    }

    protected boolean isCommentBlockStart(char c) {
        return false;
    }

    protected boolean isCommentBlockEnd(char c) {
        return false;
    }

    /**
     * Gets the next token
     * 
     * @return
     */
    public int nextToken() {
        /*
         * Always return a token, even if it is just EOL eat white space.
         */
        char c = skipSpc();

        if (isString(c)) {
            return parseString();

        } else if (isIdentifierStart(c)) {
            return parseIdentifier();

        } else if (isNumber(c)) {
            return parseNumber();

        } else if (isParen(c)) {
            return parseParen();

        } else if (isSymbol(c)) {
            return parseSymbol();

        } else {
            return eof();
        }
    }

    protected int eof() {
        val = "";
        return (tok = EOF);
    }

    protected char skipSpc() {
        char c;
        while (true) {
            c = peekc();

            if (c == 0)
                return 0;

            if (isSpace(c)) {
                getc();

            } else if (isCommentBlockStart(c)) {
                parseCommentBlock();

            } else {
                break;
            }
        }
        return c;
    }

    protected void parseCommentBlock() {
        char c;
        while ((c = peekc()) != 0 && !isCommentBlockEnd(c)) {
            getc();
        }
    }

    protected int parseString() {
        sb.setLength(0);

        // Get the quote character.
        //
        char quote = getc();
        char c;

        // Parse until the end quote.
        //
        while ((c = getc()) != 0 && c != quote) {
            if (c == '\\' && parseEscapes)
                parseEscape(quote);
            else
                sb.append(c);
        }

        val = sb.toString();
        return (tok = STRING);
    }

    private void parseEscape(char quote) {
        char c = peekc();
        if (c == quote) {
            c = getc();
            sb.append(c);
        } else {
            sb.append('\\');
        }
    }

    protected int parseIdentifier() {
        sb.setLength(0);

        sb.append(getc());
        char c;

        while ((c = peekc()) != 0 && isIdentifier(c)) {
            sb.append(c);
            getc();
        }
        val = sb.toString();
        return (tok = IDENTIFIER);
    }

    protected int parseNumber() {
        sb.setLength(0);

        sb.append(getc());
        char c;

        while ((c = peekc()) != 0) {
            if (isNumber(c) == false && c != '.')
                break;
            sb.append(c);
            getc();
        }

        val = sb.toString();
        return (tok = NUMBER);
    }

    protected int parseParen() {
        val = new String(new char[] { getc() });
        return (tok = SYMBOL);
    }

    protected int parseSymbol() {

        sb.setLength(0);

        while (true) {
            char c = getc();
            sb.append(c);

            if (istermSymbol(c))
                break;

            c = peekc();
            if (c == 0 || !isSymbol(c) || istermSymbol(c))
                break;
        }

        val = sb.toString();
        return (tok = SYMBOL);
    }

    public char getc() {
        if (sourcePos >= source.length)
            return 0;
        else
            return source[sourcePos++];
    }

    public char peekc() {
        return (sourcePos >= source.length ? 0 : source[sourcePos]);
    }

    protected char peekc(int pos) {
        return (sourcePos + pos >= source.length ? 0 : source[sourcePos + pos]);
    }

    public boolean skipc(int pos) {
        if (sourcePos + pos <= source.length) {
            sourcePos += pos;
            return true;
        }
        return false;
    }

    public String val() {
        return val;
    }

    public int token() {
        return tok;
    }

    public boolean isval(String s) {
        if (s.length() == 1 && val.length() == 1)
            return (val.charAt(0) == s.charAt(0));
        else
            return (val.equals(s));
    }

    public boolean isval(char c) {
        if (val.length() == 1)
            return (val.charAt(0) == c);
        else
            return false;
    }

    public boolean iseof() {
        return tok == EOF;
    }

    public boolean isIdentifier() {
        return tok == IDENTIFIER;
    }

    public String getParseString() {
        return new String(source);
    }
}
