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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;

public final class Urls {
    /**
     * @param url
     *            URL of the resource to read.
     * @param charsetName
     */
    public static String readUrl(URL url, String charsetName) throws java.io.IOException {
        return toString(url.openStream(), charsetName);
    }

    public static String toString(InputStream is, String charsetName) throws java.io.IOException {

        StringBuilder fileData = new StringBuilder(1000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, charsetName));
        char[] buf = new char[1024];

        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            fileData.append(buf, 0, numRead);
        }

        reader.close();
        return fileData.toString();
    }

    public static URL toUrl(Path file) {
        try {
            return file.toUri().toURL();

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static URL toUrl(String url) {
        try {
            return new URL(url);

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static URI toUri(String url) {
        try {
            return new URI(url);

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Simple function to attempt to make a url valid. many urls scrapped from
     * web sites may not contain valid characters. <br/>
     * Eg: "http://www.abc.com/product/Acer Laptop Computer" <br/>
     * The spaces in the url above should have been encoded as "%20". All modern
     * browsers will actually convert the spaces into %20 prior to making the
     * request to the server so that the server receives the correct url in the
     * request: <br/>
     * Eg: GET /product/Acer%20Laptop%20Computer <br/>
     * This method will try to make the url valid by converting some characters.
     * We may need to extend this in the future to encode other characters like
     * "[]", etc. Some characters like '/' and ':' cannot be encoded because
     * they form part of the url!
     * 
     * @param url
     * @return
     */
    public static String validifyUrl(String url) {
        char[] seq = url.toCharArray();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i != seq.length; i++) {
            char c = seq[i];
            if (c == ' ')
                sb.append("%20");
            else
                sb.append(c);
        }

        return sb.toString();
    }

    /**
     * Appends a path to a base url. Example UrlUtils.concat("http://abc.com/",
     * "/images/a.png") => "http://abc.com/images/a.png"
     * 
     * @param url
     * @param path
     *            The path to append. If the path contains "../" an
     *            {@link IllegalArgumentException} will be thrown.
     * @return
     */
    public static String concat(String url, String path) {

        // Do not allow ..
        //
        if (path.contains("../")) {
            throw new IllegalArgumentException("../ not allowed in path");
        }

        StringBuilder sb = new StringBuilder(url);
        if (url.endsWith("/") == false) {
            sb.append('/');
        }

        sb.append(path);
        return sb.toString();
    }

    public static void resolvePath(StringBuilder sb, String uri, String relativePath) {

        // "/crm/users/user" "../../" => "/crm"
        //
        int off = 0;
        while (relativePath.startsWith(".", off)) {

            // Check for "../"
            //
            if (relativePath.startsWith("../", off)) {
                // /crm/users/user "../" => "/crm/users"
                //
                int ndx = uri.lastIndexOf('/');
                if (ndx != -1) {
                    uri = uri.substring(0, ndx);
                }

                // Skip over "../"
                //
                off += 3;

            } else if (relativePath.startsWith("./", off)) {
                // Skip over "./"
                //
                off += 2;
            }
        }

        // Remove the "../" and "./" prefixes from the relative path
        //
        if (off != 0) {
            relativePath = relativePath.substring(off);
        }

        int lastPart = uri.lastIndexOf('/');
        if (lastPart != -1) {
            sb.append(uri, 0, lastPart);
        } else {
            sb.append(uri);
        }

        sb.append('/');
        sb.append(relativePath);
    }

    /**
     * @see URLEncoder#encode(String)
     * @param s
     * @return
     */
    public static String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
