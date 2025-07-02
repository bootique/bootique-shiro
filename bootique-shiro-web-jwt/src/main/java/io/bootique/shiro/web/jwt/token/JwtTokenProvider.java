package io.bootique.shiro.web.jwt.token;

import io.bootique.shiro.web.jwt.keys.JwksProvider;
import io.bootique.shiro.web.jwt.token.claim.JwtClaim;
import io.bootique.shiro.web.jwt.token.claim.StringListClaim;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.IOException;
import io.jsonwebtoken.security.Jwk;

import java.security.Key;
import java.util.List;

public class JwtTokenProvider {

    private final JwksProvider jwkProvider;
    private JwtClaim<List<String>> rolesClaim;

    public JwtTokenProvider(JwksProvider jwkProvider) {
        this.jwkProvider = jwkProvider;
    }

    public void setRolesClaim(StringListClaim claim) {
        this.rolesClaim = claim;
    }

    private Jwt<?,?> getJwt(String token) throws IOException {
        Locator<Key> keyLocator = (header) -> {
            Jwk<?> jwk = jwkProvider.getJwks().get(header.getOrDefault("kid", ""));
            if (jwk != null) {
                return jwk.toKey();
            }
            return null;
        };
        return Jwts.parser().keyLocator(keyLocator).build().parse(token);
    }

    public JwtToken getJwtToken(String token) {
        JwtToken jwtToken = new JwtToken();
        jwtToken.setRoles(rolesClaim.parse(getJwt(token).accept(Jws.CLAIMS).getPayload()));
        return jwtToken;
    }
}
