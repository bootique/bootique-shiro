package io.bootique.shiro.web.jwt;

import io.bootique.shiro.web.jwt.authz.AuthzReader;
import io.jsonwebtoken.Claims;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * @since 4.0
 */
public class JwtRealm extends AuthorizingRealm {

    private final AuthzReader rolesReader;

    public JwtRealm(AuthzReader rolesReader) {
        this.setAuthenticationTokenClass(JwtBearerToken.class);
        this.rolesReader = rolesReader;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        principalCollection
                .byType(Claims.class)
                .forEach(c -> authorizationInfo.addRoles(rolesReader.readAuthz(c)));
        return authorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
            throws AuthenticationException {
        return new SimpleAuthenticationInfo(
                authenticationToken.getPrincipal(),
                authenticationToken.getCredentials(),
                JwtRealm.class.getSimpleName());
    }
}
