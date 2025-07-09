package io.bootique.shiro.web;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AnyRoleAuthorizationFilterTest {


    @Test
    void isAccessAllowed() {
        assertTrue(new FilterTester().isAccessAllowed());
        assertTrue(new FilterTester().isAccessAllowed("r1"));
        assertTrue(new FilterTester("r1", "r2").isAccessAllowed("r1"));
        assertTrue(new FilterTester("r1", "r2").isAccessAllowed("r2", "r1"));
        assertFalse(new FilterTester("r1", "r2").isAccessAllowed("r3"));
    }

    static class FilterTester {
        private final String[] filterRoles;

        public FilterTester(String... filterRoles) {
            this.filterRoles = filterRoles;
        }

        boolean isAccessAllowed(String... subjectRoles) {

            // TODO: mocking and reimplementing real Subject role matching logic is ugly. A full integration test is in
            //  order here
            Subject subject = mock(Subject.class);
            when(subject.hasRoles(anyList())).thenAnswer(i -> hasRoles(i.getArgument(0, List.class), subjectRoles));
            ThreadContext.bind(subject);

            try {
                AnyRoleAuthorizationFilter f = new AnyRoleAuthorizationFilter();
                return f.isAccessAllowed(mock(ServletRequest.class), mock(ServletResponse.class), filterRoles);
            } finally {
                ThreadContext.unbindSubject();
            }
        }

        private static boolean[] hasRoles(List<String> filterRoles, String[] subjectRoles) {
            int slen = subjectRoles.length;
            boolean[] matches = new boolean[slen];
            for (int i = 0; i < slen; i++) {
                matches[i] = filterRoles.contains(subjectRoles[i]);
            }

            return matches;
        }
    }
}
