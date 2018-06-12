/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.shiro.mgt;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;

/**
 * A {@link RememberMeManager} implementation that disables "remember me" functionality.
 *
 * @since 0.24
 */
public class NoRememberMeManager implements RememberMeManager {

    private static final PrincipalCollection EMPTY_PRINCIPALS = new SimplePrincipalCollection();

    @Override
    public PrincipalCollection getRememberedPrincipals(SubjectContext subjectContext) {
        return EMPTY_PRINCIPALS;
    }

    @Override
    public void forgetIdentity(SubjectContext subjectContext) {
        // do nothing..
    }

    @Override
    public void onSuccessfulLogin(Subject subject, AuthenticationToken token, AuthenticationInfo info) {
        // do nothing..
    }

    @Override
    public void onFailedLogin(Subject subject, AuthenticationToken token, AuthenticationException ae) {
        // do nothing..
    }

    @Override
    public void onLogout(Subject subject) {
        // do nothing..
    }
}
