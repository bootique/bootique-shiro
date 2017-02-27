package io.bootique.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.junit.Test;
import org.mockito.Matchers;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ThreadLocalSubjectManagerTest {

    @Test
    public void testSubject() {

        ThreadLocalSubjectManager sm = new ThreadLocalSubjectManager();

        Subject mockSubject = mock(Subject.class);
        SecurityManager mockSM = mock(SecurityManager.class);
        when(mockSM.createSubject(Matchers.any(SubjectContext.class))).thenReturn(mockSubject);

        SecurityUtils.setSecurityManager(mockSM);

        assertSame(mockSubject, sm.subject());
    }

    @Test(expected = UnavailableSecurityManagerException.class)
    public void testSubject_NoSubject() {
        new ThreadLocalSubjectManager().subject();
    }
}
