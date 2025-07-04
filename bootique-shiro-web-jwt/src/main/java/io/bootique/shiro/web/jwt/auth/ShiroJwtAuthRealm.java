package io.bootique.shiro.web.jwt.auth;

import io.bootique.shiro.web.jwt.token.JwtToken;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

public class ShiroJwtAuthRealm extends AuthorizingRealm {

    public ShiroJwtAuthRealm() {
        this.setAuthenticationTokenClass(ShiroJwtAuthToken.class);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        principalCollection
                .byType(JwtToken.class)
                .forEach(p -> authorizationInfo.addRoles(p.getRoles()));
        return authorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
            throws AuthenticationException {
        return new SimpleAuthenticationInfo(
                authenticationToken.getPrincipal(),
                authenticationToken.getCredentials(),
                ShiroJwtAuthRealm.class.getSimpleName());
    }
}
