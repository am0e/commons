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
package com.github.am0e.providers;

import java.io.Closeable;
import java.io.IOException;

import com.github.am0e.msgs.Msgs;

public class ObjectProviderContext {
    private ObjectProviderContext() {
    }

    private final static class ContextData implements Closeable {
        /**
         * Previous context.
         */
        final ContextData prev;

        /**
         * Current provider in context.
         */
        final ObjectProvider objectProvider;

        public ContextData(ContextData contextData, ObjectProvider provider) {
            this.prev = contextData;
            this.objectProvider = provider;
        }

        @Override
        public void close() throws IOException {
            leaveContext();
        }
    }

    final static ThreadLocal<ContextData> contextData = new ThreadLocal<>();

    public static Closeable enterContext(ObjectProvider provider) {
        ContextData cd = new ContextData(contextData.get(), provider);
        contextData.set(cd);

        if (provider instanceof ObjectProviderContextEvents) {
            ((ObjectProviderContextEvents) provider).onEnterContext();
        }
        return cd;
    }

    public static void leaveContext() {
        ContextData o = contextData.get();

        if (o != null) {
            // Revert to previous.
            //
            contextData.set(o.prev);

            // Check for leave event.
            //
            if (o.objectProvider instanceof ObjectProviderContextEvents) {
                ((ObjectProviderContextEvents) o.objectProvider).onLeaveContext();
            }
        }
    }

    public static ObjectProvider providerInContext() {
        // Get the current provider.
        //
        ContextData o = contextData.get();

        return o == null ? null : o.objectProvider;
    }

    public static <T> T instanceOf(Class<T> type) {
        ObjectProvider o = providerInContext();

        if (o == null) {
            throw new IllegalStateException("No ObjectProvider context has been established in this thread.");

        }

        T p = o.instanceOf(type);

        if (p == null) {
            throw new IllegalArgumentException(Msgs.format("Type {} not provided by context.", type));
        }

        return p;
    }

    public static <T> T getInstanceOf(Class<T> type) {
        ObjectProvider o = providerInContext();

        if (o == null) {
            return null;
        }

        return o.instanceOf(type);
    }
}
