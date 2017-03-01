package io.bootique.shiro.subject;

import org.apache.shiro.subject.Subject;

/**
 * An injectable facade for accessing Shiro {@link Subject}. It allows to avoid the deopendency on Shiro ThreadLocals
 * or statics in the app code.
 */
public interface SubjectManager {

    /**
     * Returns currently active subject.
     *
     * @return currently active Shiro subject.
     */
    Subject subject();
}
