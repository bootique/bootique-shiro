/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.shiro.mdc;

import org.slf4j.MDC;

/**
 * A wrapper around SLF4J MDC (Mapped Diagnostic Context) class that allows to expose current principal name in the
 * logging context.
 */
public class PrincipalMDC {

    public static final String MDC_KEY = "principal";

    /**
     * Initializes SLF4J MDC with the current principal name.
     */
    public void reset(Object principal) {
        if (principal == null) {
            MDC.remove(MDC_KEY);
        } else {
            MDC.put(MDC_KEY, String.valueOf(principal));
        }
    }

    /**
     * Removes principal name from the logging MDC.
     */
    public void clear() {
        MDC.remove(MDC_KEY);
    }
}
