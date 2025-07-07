package io.bootique.shiro.web.jwt.authz;

import io.jsonwebtoken.Claims;

import java.util.List;

/**
 * @since 4.0
 */
public interface AuthzReader {

    List<String> readAuthz(Claims claims);
}
