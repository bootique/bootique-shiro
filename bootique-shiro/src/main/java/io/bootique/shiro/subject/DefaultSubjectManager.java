package io.bootique.shiro.subject;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;

/**
 * A factory of subjects. Does not attempt to cache or reuse existing subjects.
 */
public class DefaultSubjectManager implements SubjectManager {

    private SecurityManager securityManager;

    public DefaultSubjectManager(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    @Override
    public Subject subject() {
        return new Subject.Builder(securityManager).buildSubject();
    }
}
