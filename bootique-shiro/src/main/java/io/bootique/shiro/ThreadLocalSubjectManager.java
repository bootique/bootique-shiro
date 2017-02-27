package io.bootique.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;

/**
 * A default implementation of {@link SubjectManager} that uses standard Shiro lookup using {@link SecurityUtils}.
 * Requires the external code to setup {@link org.apache.shiro.mgt.SecurityManager} either via the
 * {@link org.apache.shiro.util.ThreadState} or staticly via {@link SecurityUtils#setSecurityManager(SecurityManager)}.
 */
public class ThreadLocalSubjectManager implements SubjectManager {

    @Override
    public Subject subject() {
        return SecurityUtils.getSubject();
    }
}
