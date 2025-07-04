package io.bootique.shiro.web.jwt.token;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JwtToken {

    private final Set<String> roles;

    public JwtToken(List<String> roles) {
        this.roles = roles == null ? Collections.emptySet() : new HashSet<>(roles);
    }

    public Set<String> getRoles() {
        return roles;
    }
}
