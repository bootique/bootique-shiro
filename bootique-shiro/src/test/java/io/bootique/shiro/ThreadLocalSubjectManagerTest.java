package io.bootique.shiro;

import io.bootique.shiro.subject.DefaultSubjectManager;
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

        Subject mockSubject = mock(Subject.class);
        SecurityManager mockSM = mock(SecurityManager.class);
        when(mockSM.createSubject(Matchers.any(SubjectContext.class))).thenReturn(mockSubject);

        DefaultSubjectManager sm = new DefaultSubjectManager(mockSM);
        assertSame(mockSubject, sm.subject());
    }
}
