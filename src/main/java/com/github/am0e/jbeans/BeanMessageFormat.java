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
package com.github.am0e.jbeans;

/**
 * Simple class to expand field references in a source string from bean fields.
 * For example:
 * 
 * <pre>
 * format("Name is %{name} age is %{age}", bean);
 * </pre>
 */
public class BeanMessageFormat {

    private BeanAccessor wrapper;

    protected Object getBeanField(String name) {
        return wrapper.get(name);
    }

    public BeanMessageFormat(BeanAccessor accessor) {
        this.wrapper = accessor;
    }

    public BeanMessageFormat(Object bean) {
        this.wrapper = new BeanAccessor(bean);
    }

    public void setBean(Object bean) {
        wrapper.setBean(bean);
    }

    /**
     * Simple method to expand field references in a source string from bean
     * fields. For example:
     * 
     * <pre>
     * format("Name is %{name} age is %{age}", bean);
     * </pre>
     * 
     * @param source
     * @param bean
     * @return
     */
    public String format(String s) {

        char[] chars = s.toCharArray();
        StringBuilder sb = new StringBuilder();

        for (int pos = 0; pos != chars.length; pos++) {

            if (chars[pos] == '%' && (pos + 1 != chars.length) && chars[pos + 1] == '{') {
                pos += 2;
                int st = pos;
                while (pos != chars.length && chars[pos] != '}') {
                    pos++;
                }

                String key = new String(chars, st, pos - st);
                sb.append(getBeanField(key));

            } else {
                sb.append(chars[pos]);
            }
        }

        return sb.toString();
    }

    /**
     * public final static String replace(String source, Object bean) { final
     * BeanClassWrapper cw = new BeanClassWrapper(bean.getClass()); final Object
     * beanContext = bean;
     * 
     * StrLookup lookup = new StrLookup() { public String lookup(String key) {
     * Object o = cw.callGetter(beanContext, key); return o==null ? "" :
     * o.toString(); } };
     * 
     * StrSubstitutor s = new StrSubstitutor(lookup); return s.replace(source);
     * }
     */
}
