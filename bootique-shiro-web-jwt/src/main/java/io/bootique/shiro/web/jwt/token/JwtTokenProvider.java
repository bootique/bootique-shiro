package io.bootique.shiro.web.jwt.token;

import io.bootique.shiro.web.jwt.keys.JwksProvider;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.IOException;
import io.jsonwebtoken.security.Jwk;

import java.security.Key;
import java.util.List;

public class JwtTokenProvider {

    private final JwksProvider jwkProvider;
    private final JwtTokenClaim rolesClaim;

    JwtTokenProvider(JwksProvider jwkProvider,JwtTokenClaim roles) {
        this.jwkProvider = jwkProvider;
        this.rolesClaim = roles;
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
        getJwt(token).accept(Jws.CLAIMS).getPayload().forEach((key, value) -> {
            if (key.equals(rolesClaim.getName())) {
                jwtToken.setRoles(rolesClaim.parse(value));
            }
        });
        return jwtToken;
    }
}
