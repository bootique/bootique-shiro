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
package io.bootique.shiro.web;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;

import java.util.List;

/**
 * An authorizing filter that works similar to the "roles" filter, except the presence of any one role is sufficient
 * to authorize a user.
 *
 * @since 4.0
 */
public class AnyRoleAuthorizationFilter extends AuthorizationFilter {

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        Subject subject = getSubject(request, response);
        String[] roles = (String[]) mappedValue;

        if (roles == null || roles.length == 0) {
            // no roles specified, so nothing to check - allow access.
            return true;
        }

        // TODO: would it be faster to check roles individually and bail early on a match? Shiro seems to think
        //  otherwise, mentioning that Subject.hasRole(..) may result in a remote call in some cases
        boolean[] matches = subject.hasRoles(List.of(roles));
        for (int i = 0; i < matches.length; i++) {
            if (matches[i]) {
                return true;
            }
        }

        return false;
    }
}
