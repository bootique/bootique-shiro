package io.bootique.shiro.web.subject;

import io.bootique.shiro.subject.SubjectManager;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class DefaultWebSubjectManager implements SubjectManager {

    @Override
    public Subject subject() {

        // Presumably subject is already available as a ThreadLocal, installed by AbstractShiroFilter...
        // Creating it ourselves is probably (1) wrong in the context of AbstractShiroFilter and
        // (2) not very easy as WebSubject.Builder requires access to both request and response and
        // WebEnvironment only provides the former...

        return SecurityUtils.getSubject();
    }
}
