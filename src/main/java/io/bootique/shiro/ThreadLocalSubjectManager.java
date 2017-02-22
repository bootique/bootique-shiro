package io.bootique.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class ThreadLocalSubjectManager implements SubjectManager {

    @Override
    public Subject subject() {
        return SecurityUtils.getSubject();
    }
}
