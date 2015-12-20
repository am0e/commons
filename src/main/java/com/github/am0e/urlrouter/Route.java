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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.am0e.lib.AntLib;
import com.github.am0e.utils.Handler;

/**
 * Represents a part of a url. For example a url "/api/users" will have 2
 * routes "/api" and "/users" The parent of "/users" being "/api"
 * <p>
 * routes can have arbitrary attributes which can be used to assign security,
 * etc to the route.
 * <p>
 * routes can have handlers which typically define the controllers for the
 * route.
 * <p>
 * routes can represent variables. Eg "/api/users/[:id]". The route for ":id"
 * is a variable.
 * 
 * @author anthony
 *
 */
public final class Route {

    /**
     * The path pattern. Eg: "catalog" or if the part is a variable name, the
     * name of the variable. Eg the path value for "/[:id]" is "id"
     */
    private char[] path;

    /**
     * Set of instantiated attributes associated with the URL.
     */
    Object[] attributes = emptyArray;

    /**
     * The optional route handler
     */
    RouteHandler<?> routeHandler = null;

    /**
     * The optional context handlers for the route.
     */
    ContextHandler<?> contextHandlers[] = emptyHandlers;

    /**
     * The parent route.
     */
    Route parent;

    /**
     * Child routes.
     */
    Route[] children;
    int childrenSize;

    /**
     * Match Flags
     */
    byte flags;

    /**
     * The required protocol eg http, https, any null means get protocol from
     * parent.
     */
    String protocol;

    final static byte F_STAR = 1;
    final static byte F_SET_PARAM = 2;

    private final static Object[] emptyArray = new Object[0];
    private final static Route[] emptyRoutesArray = new Route[0];
    private final static ContextHandler<?>[] emptyHandlers = new ContextHandler<?>[0];

    public static final String PROTOCOL_HTTP = "http";
    public static final String PROTOCOL_HTTPS = "https";
    public static final String PROTOCOL_ANY = "any";

    public Route() {
        this.children = emptyRoutesArray;
    }

    public Route(String path) {
        this();

        if (path.startsWith(":")) {
            // Variable:
            // :name
            //
            path = path.substring(1);
            flags |= F_SET_PARAM;
        }

        if (path.endsWith("*")) {
            // :name*
            //
            path = path.substring(0, path.length() - 1);
            flags |= F_STAR;
        }

        this.path = path.toCharArray();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (parent != null) {
            sb.append('/');
        }

        if ((flags & F_SET_PARAM) != 0) {
            sb.append(':');
            sb.append(path);
        } else {
            sb.append(path);
        }

        if ((flags & F_STAR) != 0)
            sb.append("*");

        if (parent != null) {
            sb.insert(0, parent.toString());
        }
        return sb.toString();
    }

    public final boolean matches(char[] part) {
        if (path[0] == '?')
            return true;
        else
            return Arrays.equals(path, part);
    }

    public final String path() {
        return new String(path);
    }

    public final String getUrl(String prefix) {
        StringBuilder sb = new StringBuilder();

        List<Route> parents = AntLib.newList(5);

        for (Route it = this; it != null; it = it.parent) {
            parents.add(it);
        }

        for (int i = parents.size() - 2; i >= 0; i--) {
            sb.append('/');
            sb.append(parents.get(i).path);
        }

        if (prefix != null) {
            if (prefix.charAt(0) != '/' && prefix.charAt(0) != '?')
                sb.append('/');

            sb.append(prefix);
        }

        return sb.toString();
    }

    public final <T> T getAttr(Class<T> type) {
        return getAttr(type, true);
    }

    /**
     * Locates an attribute of the specified type. If this route does not have
     * the attribute, the parent is checked and so on upto the root.
     * 
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    public final <T> T getAttr(Class<T> type, boolean searchParent) {
        for (Route it = this; it != null; it = it.parent) {
            for (Object a : it.attributes) {
                if (type.isInstance(a))
                    return (T) a;
            }
            if (searchParent == false)
                break;
        }

        return null;
    }

    public final RouteAttr getAttr(String name, boolean searchParent) {
        for (Route it = this; it != null; it = it.parent) {
            for (Object a : it.attributes) {
                if (a instanceof RouteAttr) {
                    RouteAttr attr = (RouteAttr) a;
                    if (attr.getName().equals(name)) {
                        return attr;
                    }
                }
                if (searchParent == false)
                    break;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> RouteHandler<T> routeHandler() {
        return (RouteHandler<T>) this.routeHandler;
    }

    public Route setRouteHandler(RouteHandler<?> handler) {
        this.routeHandler = handler;
        return this;
    }

    public Route addContextHandler(ContextHandler<?> handler) {
        this.contextHandlers = Arrays.copyOf(this.contextHandlers, this.contextHandlers.length + 1);
        this.contextHandlers[this.contextHandlers.length - 1] = handler;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> ContextHandler<T>[] contextHandlers() {
        return (ContextHandler<T>[]) this.contextHandlers;
    }

    public boolean hasRouteHandler() {
        return (routeHandler == null) ? false : true;
    }

    public final void setAttributes(Collection<?> attributes) {
        this.attributes = attributes.toArray(new Object[0]);
    }

    public Route getChild(int pos) {
        return children[pos];
    }

    public int getChildSize() {
        return childrenSize;
    }

    /**
     * Finds a child route that has the specified path name.
     * 
     * @param path
     * @return
     */
    public Route getChild(String path) {
        char[] part = path.toCharArray();

        for (int i = 0; i != childrenSize; i++) {
            if (children[i].matches(part))
                return children[i];
        }

        return null;
    }

    /**
     * Adds a child to this route.
     */
    public void addChild(Route child) {
        ensureCapacity(childrenSize + 1);
        children[childrenSize++] = child;
        child.parent = this;
    }

    void ensureCapacity(int minCapacity) {
        int oldCapacity = children.length;
        if (minCapacity > oldCapacity) {
            Object oldData[] = children;
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity)
                newCapacity = minCapacity;
            children = new Route[newCapacity];
            System.arraycopy(oldData, 0, children, 0, childrenSize);
        }
    }

    public final String protocol() {
        return (protocol == null && parent != null) ? parent.protocol() : protocol;
    }

    public final Route setProtocol(String protocol) {
        if (protocol.equals("http"))
            this.protocol = PROTOCOL_HTTP;
        else if (protocol.equals("https"))
            this.protocol = PROTOCOL_HTTPS;
        else if (protocol.equals("any"))
            this.protocol = PROTOCOL_ANY;
        else if (StringUtils.isEmpty(protocol))
            this.protocol = null;
        else
            this.protocol = protocol;

        return this;
    }

    public Route parent() {
        return parent;
    }

    /**
     * Returns an array of Handlers. The array includes the context handlers ordered from the
     * top most route first down to the actual route. The route handler for the route is the last
     * entry in the array.
     * 
     * @param includeRouteHandler  Include the actual route handler. If false, the returned array will
     * only contain the context handlers.
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> Handler<T>[] allHandlers(boolean includeRouteHandler) {
        List<Handler<?>> list = new ArrayList<>();

        if (routeHandler != null && includeRouteHandler) {
            list.add(routeHandler);
        }

        for (Route it = this; it != null; it = it.parent) {
            for (int i = it.contextHandlers.length - 1; i >= 0; i--) {
                list.add(it.contextHandlers[i]);
            }
        }

        Handler<?>[] ar = list.toArray(new Handler<?>[0]);
        ArrayUtils.reverse(ar);

        return (Handler<T>[]) ar;
    }

    public Object[] attributes() {
        return attributes;
    }
}
