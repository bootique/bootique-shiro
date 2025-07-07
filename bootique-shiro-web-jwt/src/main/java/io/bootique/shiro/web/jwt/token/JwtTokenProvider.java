package io.bootique.shiro.web.jwt.token;

import io.bootique.shiro.web.jwt.keys.JwksProvider;
import io.bootique.shiro.web.jwt.token.claim.JwtClaim;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.IOException;
import io.jsonwebtoken.security.Jwk;

import java.security.Key;
import java.util.List;

public class JwtTokenProvider {

    private final JwksProvider jwkProvider;
    private final JwtClaim<?,?> rolesClaim;

    public JwtTokenProvider(JwksProvider jwkProvider, JwtClaim<?,?> rolesClaim) {
        this.jwkProvider = jwkProvider;
        this.rolesClaim = rolesClaim;
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

    @SuppressWarnings("unchecked")
    public JwtToken getJwtToken(String token) {
        Claims claims = getJwt(token).accept(Jws.CLAIMS).getPayload();
        List<String> roles = (List<String>) rolesClaim.parse(claims);
        return new JwtToken(roles);
    }
}
