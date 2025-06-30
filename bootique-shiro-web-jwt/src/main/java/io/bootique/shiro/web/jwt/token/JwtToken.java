package io.bootique.shiro.web.jwt.token;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JwtToken {

    private Set<String> roles;

    public Set<String> getRoles() {
        return roles == null ? Collections.emptySet() : roles;
    }

    void setRoles(List<String> roles) {
        this.roles = new HashSet<>(roles);
    }
}
