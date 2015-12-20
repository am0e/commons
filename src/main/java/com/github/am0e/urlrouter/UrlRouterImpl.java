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

import org.apache.commons.lang3.StringUtils;

/**
 * Collection of {@link Route} objects.
 * 
 * @author anthony
 */
public class UrlRouterImpl implements UrlRouter {

    /**
     * Root mapping for "/"
     */
    private Route root = new Route("/");

    /**
     * For servlets, an optional context path including the servlet path. Eg
     * /mycontext/myservlet.
     * <p>
     * If the paths in the mappings do not contain the context and servlet
     * paths, specify the context path in the mappings configuration file using.
     * <p>
     * For example: <br>
     * {@code <context-path value="/mywebapp/myservlet"/>}
     * <p>
     * This will allow the path with or without the context/servlet path to
     * resolve to a controller. For example both
     * "/mywebapp/myservlet/customers/1234" and "/customers/1234" will resolve
     * to CustomersController.
     */
    private String contextPath;

    /**
     * Default host settings. Refers to a{@link UrlHost} object by it's id.
     */
    private String defaultHost;

    /*
     * (non-Javadoc)
     * 
     * @see com.github.am0e.urlrouter.UrlRouter#getRoot()
     */
    @Override
    public final Route root() {
        return root;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.github.am0e.urlrouter.UrlRouter#validate()
     */
    @Override
    public void validate() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.github.am0e.urlrouter.UrlRouter#normalizePath(java.lang.String)
     */
    @Override
    public String normalizePath(String path) {
        if (contextPath != null && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return path;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.github.am0e.urlrouter.UrlRouter#getContextPath()
     */
    @Override
    public String contextPath() {
        return contextPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.github.am0e.urlrouter.UrlRouter#setContextPath(java.lang.String)
     */
    @Override
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.github.am0e.urlrouter.UrlRouter#getDefaultHost()
     */
    @Override
    public String defaultHost() {
        return defaultHost;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.github.am0e.urlrouter.UrlRouter#setDefaultHost(java.lang.String)
     */
    @Override
    public void setDefaultHost(String defaultHost) {
        this.defaultHost = defaultHost;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.github.am0e.urlrouter.UrlRouter#addRoute(java.lang.String,
     * com.github.am0e.webc.routes.RouteHandler)
     */
    @Override
    public Route setRouteHandler(String path, RouteHandler<?> handler) {
        return addRoute(path).setRouteHandler(handler);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.github.am0e.urlrouter.UrlRouter#addRoute(java.lang.String)
     */
    @Override
    public Route addRoute(String path) {
        return addRoute(root, path);
    }

    @Override
    public Route addRoute(Route parent, String path) {

        // Split path into it's parts.
        //
        String[] parts = StringUtils.split(path, '/');
        int len = parts.length - 1;

        String name = parts[parts.length - 1];

        // Set the route path to the last part
        //
        Route route = new Route(name);

        // Starting at the root. find the insertion point for this path.
        //
        int i = 0;
        for (; i != len; i++) {
            Route child = parent.getChild(parts[i]);
            if (child == null) {
                break;
            }

            parent = child;
        }

        if (i != len) {
            // Construct the parent route entries.
            // Eg: "/catalog/orders/list/all
            // In the above we may need to construct "catalog" -> "orders" ->
            // "list"
            //
            for (; i != len; i++) {
                // Construct a new route object and add to parent.
                //
                Route m = new Route(parts[i]);
                parent.addChild(m);
                parent = m;
            }
        }

        // Do not allow duplicates.
        //
        Route existing = parent.getChild(name);

        if (existing != null) {
            return existing;
        } else {
            // Add this route to it's parent context.
            //
            parent.addChild(route);
            return route;
        }
    }

    @Override
    public Route addContextHandler(String path, ContextHandler<?> handler) {
        return addRoute(path).addContextHandler(handler);
    }

    @Override
    public RouteResolver resolver() {
        return new RouteResolver(this);
    }
}
