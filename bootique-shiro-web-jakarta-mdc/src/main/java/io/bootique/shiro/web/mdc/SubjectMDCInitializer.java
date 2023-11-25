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

package io.bootique.shiro.web.mdc;

import io.bootique.shiro.mdc.PrincipalMDC;
import jakarta.servlet.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.io.IOException;

/**
 * A Shiro filter that initializes MDC state from the current Subject. It needs to be explicitly placed in the
 * authentication chain in the "shiroweb.urls" configuration under the name "mdc". This filter is optional and is only
 * needed in session-based apps that do not perform login on every request.
 */
public class SubjectMDCInitializer implements Filter {

    private final PrincipalMDC principalMDC;

    public SubjectMDCInitializer(PrincipalMDC principalMDC) {
        this.principalMDC = principalMDC;
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        initMDC();
        chain.doFilter(request, response);
    }

    protected void initMDC() {
        Subject subject = SecurityUtils.getSubject();
        Object principal = subject.getPrincipals().getPrimaryPrincipal();
        principalMDC.reset(principal);
    }
}
