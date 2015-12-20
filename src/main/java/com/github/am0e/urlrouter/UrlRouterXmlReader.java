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

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import com.github.am0e.jbeans.BeanClassWrapper;
import com.github.am0e.jbeans.BeanUtils;
import com.github.am0e.lib.AntLib;
import com.github.am0e.msgs.Msgs;
import com.github.am0e.xmlp.model.Attribute;
import com.github.am0e.xmlp.model.Attributes;
import com.github.am0e.xmlp.model.Document;
import com.github.am0e.xmlp.model.Element;
import com.github.am0e.xmlp.parser.SimpleDomParser;

/**
 * Loads routes from an XML configuration file.
 * 
 * @author anthony
 *
 */
public final class UrlRouterXmlReader implements UrlRouteAttrInitCtx {
    private Map<String, Class<?>> attributeTypes;
    private UrlRouter routes;
    private BeanClassWrapper cd = new BeanClassWrapper();

    public UrlRouterXmlReader() {
        this.attributeTypes = AntLib.newHashMap();
    }

    public UrlRouter loadFromFile(Path file, UrlRouter routes) throws IOException {
        try (Reader rdr = Files.newBufferedReader(file)) {
            return loadFromReader(file.toString(), rdr, routes);
        }
    }

    public UrlRouter loadFromReader(String resourceName, Reader rdr, UrlRouter routes) throws IOException {
        SimpleDomParser parser = new SimpleDomParser();
        Document doc = parser.parse(resourceName, rdr);
        this.routes = routes;
        processDocument(doc);
        routes.validate();
        return routes;
    }

    public UrlRouterXmlReader addAttribute(String name, Class<?> type) {
        attributeTypes.put(name, type);
        return this;
    }

    private void processDocument(Document doc) {

        Element e = doc.getDocumentElement();

        for (Element it : e.getChildElements()) {
            switch (it.getLocalName()) {
            case "attribute":
                processAttributeType(it);
                break;
            case "route":
                processRoute(null, it);
                break;
            case "context-path":
                routes.setContextPath(it.getAttributeValue("value"));
                break;
            case "default-host":
                routes.setDefaultHost(it.getAttributeValue("value"));
                break;
            default:
                throw error("Unknown element `{}`", e.getLocalName());
            }
        }
    }

    private void processAttributeType(Element prop) {
        String name = prop.getAttributeValue("name");
        String className = prop.getAttributeValue("class");

        Class<?> claz = BeanUtils.loadClass(Thread.currentThread().getContextClassLoader(), className);

        attributeTypes.put(name, claz);
    }

    public Route processRoute(Route parent, Element routeElem) {

        String path = routeElem.getAttributeValue("path");
        String protocol = routeElem.getAttributeValue("protocol");
        Route route;

        // Check for root.
        //
        if (parent == null) {
            if (path.equals("/")) {
                route = routes.root();
            } else {
                route = routes.addRoute(path);
            }
        } else {
            route = routes.addRoute(parent, path);
        }

        route.setProtocol(protocol);
        getAttributes(route, routeElem);

        // Process child elements.
        //
        for (Element prop : routeElem.getElements("route")) {
            processRoute(route, prop);
        }

        return route;
    }

    void getAttributes(Route route, Element routeElem) {

        ArrayList<Object> attrs = new ArrayList<>(Arrays.asList(route.attributes()));

        for (Element e : routeElem.getChildElements()) {
            String localName = e.getLocalName();

            // Get the class corresponding to the attribute based on the name of
            // the node.
            //
            if (localName.equals("route")) {
                continue;
            }

            Class<?> claz = attributeTypes.get(localName);

            if (claz == null) {
                String className = e.getAttributeValue("class");
                if (className != null) {
                    claz = BeanUtils.loadClass(Thread.currentThread().getContextClassLoader(), className);
                }

                if (claz == null) {
                    throw error("Unknown attribute `{}`", e.getLocalName());
                }
            }

            // Create a wrapper for setting the properties.
            //
            cd.setClass(claz);

            // Create the bean and set the properties using attributes in the
            // element.
            //
            Object bean = cd.newInstance();
            setBeanFromAttributes(cd, bean, e);

            if (bean instanceof UrlRouteAttr) {
                // Give the attribute a chance to do more initialisation & check
                // for errors
                //
                ((UrlRouteAttr) bean).init(this, e);
            }

            if (bean instanceof RouteHandler<?>) {
                route.setRouteHandler((RouteHandler<?>) bean);

            } else if (bean instanceof ContextHandler<?>) {
                route.addContextHandler((ContextHandler<?>) bean);

            } else {
                attrs.add(bean);
            }
        }

        route.setAttributes(attrs);
    }

    @Override
    public RuntimeException error(String fmt, Object... args) {
        return new RuntimeException(Msgs.format(fmt, args));
    }

    private void setBeanFromAttributes(BeanClassWrapper cd, Object bean, Element e) {
        Attributes attributes = e.getAttributes();

        for (int i = 0; i != attributes.size(); i++) {
            Attribute node = attributes.get(i);

            String fieldName = node.getQname().replace('-', '_');
            Object value = node.getValue();

            cd.callSetter(bean, fieldName, value);
        }
    }
}
