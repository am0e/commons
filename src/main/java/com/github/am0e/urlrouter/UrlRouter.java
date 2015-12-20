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

public interface UrlRouter {

    /**
     * Retrive the root of all routes. Ie the "/" route.
     * 
     * @return
     */
    Route root();

    void validate();

    String normalizePath(String path);

    /**
     * @return the contextPath
     */
    String contextPath();

    /**
     * @param contextPath
     *            the contextPath to set
     */
    void setContextPath(String contextPath);

    /**
     * @return the defaultHost
     */
    String defaultHost();

    /**
     * @param defaultHost
     *            the defaultHost to set
     */
    void setDefaultHost(String defaultHost);

    /**
     * Sets a route handler against a path.
     * 
     * @param path
     * @param handler
     * @return
     */
    Route setRouteHandler(String path, RouteHandler<?> handler);

    /**
     * Adds a context handler to the path.
     * 
     * @param path
     * @param handler
     * @return
     */
    Route addContextHandler(String path, ContextHandler<?> handler);

    /**
     * Adds a route.
     * 
     * @param path
     * @return
     */
    Route addRoute(String path);

    /**
     * Adds a relative route to the parent.
     * 
     * @param parent
     * @param path
     * @return
     */
    Route addRoute(Route parent, String path);

    /**
     * Get the route resolver.
     * 
     * @return
     */
    RouteResolver resolver();
}