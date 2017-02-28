package io.bootique.shiro.realm;

import org.apache.shiro.realm.Realm;

import java.util.List;
import java.util.Objects;

/**
 * A holder of a final collection of Shiro {@link org.apache.shiro.realm.Realm} objects to be used in
 * {@link org.apache.shiro.mgt.SecurityManager}.
 */
public class Realms {

    // the collection is ordered...
    private List<Realm> realms;

    public Realms(List<Realm> realms) {
        this.realms = Objects.requireNonNull(realms);
    }

    public List<Realm> getRealms() {
        return realms;
    }
}
