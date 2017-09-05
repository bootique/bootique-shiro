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
