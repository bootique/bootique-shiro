package io.bootique.shiro.web.jwt.token;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.shiro.web.jwt.keys.JwksProviderFactory;

@BQConfig("Configuration of JWT Token Provider")
public class JwtTokenProviderFactory {

    private static final JwtTokenClaimFactory DEFAULT_ROLES_CLAIM;

    static {
        DEFAULT_ROLES_CLAIM = new JwtTokenClaimFactory();
        DEFAULT_ROLES_CLAIM.setName("roles");
    }

    private JwksProviderFactory jwk;

    private JwtTokenClaimFactory rolesClaim;

    @BQConfigProperty("JWK Configuration")
    public void setJwk(JwksProviderFactory jwk) {
        this.jwk = jwk;
    }

    @BQConfigProperty("JWT Token roles claim")
    public void setRolesClaim(JwtTokenClaimFactory rolesClaim) {
        this.rolesClaim = rolesClaim;
    }

    private JwksProviderFactory getJwk() {
        if (jwk == null) {
            throw new IllegalStateException("Jwk configuration is not defined");
        }
        return jwk;
    }

    private JwtTokenClaimFactory getRolesClaim() {
        return rolesClaim == null ? DEFAULT_ROLES_CLAIM : rolesClaim;
    }

    public JwtTokenProvider provideJwt() {
        return new JwtTokenProvider(getJwk().provideJwk(), getRolesClaim().provideClaim());
    }
}
