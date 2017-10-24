package io.bootique.shiro.web.mdc;

import io.bootique.shiro.mdc.PrincipalMDC;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * @since 0.25
 */
public class PrincipalMDCInitializer implements AuthenticationListener {

    PrincipalMDC principalMDC;

    public PrincipalMDCInitializer(PrincipalMDC principalMDC) {
        this.principalMDC = principalMDC;
    }

    @Override
    public void onSuccess(AuthenticationToken token, AuthenticationInfo info) {
        // TODO: will this mess things up if authentication happens outside of a web request? Should we set some request
        // attribute or a ThreadLocal from within PrincipalMDCCleaner.requestInitialized(..) and check it here?

        Object principal = info.getPrincipals().getPrimaryPrincipal();
        principalMDC.reset(principal);
    }

    @Override
    public void onFailure(AuthenticationToken token, AuthenticationException ae) {
        // do nothing... should we clear the MDC here?
    }

    @Override
    public void onLogout(PrincipalCollection principals) {
        principalMDC.clear();
    }
}
