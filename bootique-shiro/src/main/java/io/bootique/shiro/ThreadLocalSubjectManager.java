package io.bootique.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

/**
 * A default implementation of {@link SubjectManager} that uses standard Shiro lookup using {@link SecurityUtils}.
 */
public class ThreadLocalSubjectManager implements SubjectManager {

    public ThreadLocalSubjectManager() {
    }

    @Override
    public Subject subject() {
        return SecurityUtils.getSubject();
    }
}
