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
package com.github.am0e.msgs;

import java.util.Locale;

/**
 * Raw access to message sources for resolving codes into locale independent
 * strings. Higher level services will typically hide the locale and expose the
 * {@link MessageSourceContext} so that the locale does not need to be passed
 * around.
 * 
 * @see MessageSourceContext
 * @see MessageSourceResolvable
 * @author Anthony
 *
 */
public interface MessageSource {
    String getMessage(Locale locale, String code, Object... args);

    String getMessageDefault(Locale locale, String code, String defaultMessage, Object... args);
}
