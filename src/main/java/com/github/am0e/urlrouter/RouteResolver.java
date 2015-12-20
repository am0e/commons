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
package com.github.am0e.urlrouter;

import java.util.Arrays;

import com.github.am0e.utils.MultiValueMap;

/**
 * Class for resolving urls to {@link Route} objects.
 * 
 * @author anthony
 *
 */
public class RouteResolver {

    private final UrlRouter router;
    private MultiValueMap params;
    private int pos;
    private Route matchedContext;
    private Route matchedRoute;
    private char[][] parts;

    public RouteResolver(UrlRouter router) {
        this.router = router;
    }

    public MultiValueMap params() {
        return params;
    }

    public Route matchedContext() {
        return matchedContext;
    }

    public Route matchedRoute() {
        return matchedRoute;
    }

    /**
     * Gets the route associated with the path.
     * If the route is a rest style url, the _action and id parameters will be
     * updated in the params object.
     * 
     * @param path  The path to get.
     * @param from  Optional context to resolve from.
     * @param params    The params object. For RESTFul urls, the _action and id parameters may
     * be stored in the params object.
     * @param allowByName Allow name mapping if path starts with "#"
     * @return
     */
    public Route resolve(String path, Route from, MultiValueMap params, boolean allowByName) {

        if (params == null)
            params = new MultiValueMap();

        if (from==null)
            from = router.root();
        
        this.params = params;
        this.matchedRoute = null;
        this.matchedContext = null;
        this.pos = -1;

        if (allowByName && path.startsWith("#")) {
            path = from.getAttr(path.substring(1), true).getStringValue();
        }

        path = router.normalizePath(path);

        // Setup url
        //
        this.parts = getParts(path);

        // Match from the specified path.
        //
        doMatch(from);

        return matchedRoute;
    }

    protected char[][] getParts(String url) {

        // Split url by '/' ignoring empty parts.
        // eg //abc//def// -> [abc, def]
        //
        char[] chars = url.toCharArray();
        int len = chars.length;

        // Max 20 levels deep.
        //
        char[][] parts = new char[20][];
        int partIndex = 0;
        int from = 0;

        for (int i = 0; i <= len; i++) {
            if (i == chars.length || chars[i] == '/') {
                if (i > from) {
                    if (i != from + 1 && partIndex != parts.length) {
                        char[] part = Arrays.copyOfRange(chars, from + 1, i);
                        parts[partIndex++] = part;
                    }
                    from = i;
                }
            }
        }

        return Arrays.copyOf(parts, partIndex);
    }

    protected void setContextParam(String name, char[] value) {
        String v = value[0] == '-' ? new String(value, 1, value.length - 1) : new String(value);
        setContextParam(name, v);
    }

    protected void setContextParam(String name, String value) {
        params.set(name, value);
    }

    protected boolean doMatch(Route route) {

        boolean matched = false;
        matchedContext = route;

        // Have => /products/? <= Canonical controller, action, id
        // Url => /products/edit/12400094
        //
        // Have /products/?/edit
        // Url /products/Olympus+ZF+Camera/edit

        // First match in child specific, if any.
        //
        if ((pos + 1) < parts.length && route.childrenSize != 0) {
            // Next part in url to match.
            //
            pos++;

            // Look for match in a child.
            //
            for (int i = 0; i != route.childrenSize; i++) {
                if (doMatchChild(route.children[i])) {
                    matched = true;
                    break;
                }
            }

            // Restore pos.
            //
            pos--;

            // Did we match in a child?
            //
            if (matched) {
                return true;
            }
        }

        // No match in child, can we match in the current route?
        // Only if the route has a handler.
        //
        if (route.routeHandler != null) {
            // We have a canonical controller:
            // controller/
            // controller/<id>
            // controller/<id>/<action>
            //
            if (matchIdAndAction(pos + 1)) {
                // Matched.
                // Return back up the stack.
                //
                this.matchedRoute = route;
                matched = true;
            }
        }

        // No match at this level.
        //
        return matched;
    }

    protected boolean doMatchChild(Route c) {

        final byte flags = c.flags;

        if ((flags & Route.F_STAR) != 0) {
            // Have:
            // route="/category/:path*" Url="/category/main/cameras/digital"
            // Set param "path" to "main/cameras/digital"
            //
            if ((flags & Route.F_SET_PARAM) != 0) {
                setContextParam(c.path(), getRemaining());
            }
            this.matchedRoute = c;
            return true;
        }

        if ((flags & Route.F_SET_PARAM) != 0) {
            // Have:
            // route="/products/[name]" url="/products/12334"
            // paramName = "name"
            // url = "12334"
            //
            if (doMatch(c)) {
                setContextParam(c.path(), parts[pos]);
                return true;
            }

        } else if (c.matches(parts[pos])) {
            // matched part.
            //
            if (doMatch(c)) {
                return true;
            }
        }

        return false;
    }

    protected boolean matchIdAndAction(int pos) {

        char[] id = null;
        char[] action = null;

        // Here we allow:
        // /
        // /id
        // /id/action
        //
        if (pos < parts.length) {

            // Match
            // <id>
            // <id>/<action>
            //
            if (isId(parts[pos])) {
                // Have <id>
                // Have <id>/<action>
                //
                id = parts[pos++];

                if (pos < parts.length && !isId(parts[pos])) {
                    // Have <action>
                    //
                    action = parts[pos++];
                }

            } else {
                // Have old form (deprecated):
                // Have <action>
                // Have <action>/<id>
                //
                action = parts[pos++];

                if (pos < parts.length) {
                    if (isId(parts[pos])) {
                        // Have /products/edit/12345
                        //
                        id = parts[pos++];
                    }
                }
            }
        }

        // Did we arrive at end of path?
        // Ie the following is rejected
        // /id/action/extra/extra
        //
        if (pos == parts.length) {
            if (id != null) {
                setContextParam("id", id);
            }
            if (action != null) {
                setContextParam("_action", action);
            }

            return true;
        }

        return false;
    }

    protected boolean isId(char[] cs) {
        return ((cs[0] >= '0' && cs[0] <= '9') || cs[0] == '-');
    }

    protected String getRemaining() {
        StringBuilder sb = new StringBuilder();

        for (int i = pos; i < parts.length; i++) {

            if (i != pos)
                sb.append('/');

            sb.append(parts[i]);
        }

        return sb.toString();
    }
}
