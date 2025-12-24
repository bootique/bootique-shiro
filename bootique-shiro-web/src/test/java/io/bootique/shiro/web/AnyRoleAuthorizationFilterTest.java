package io.bootique.shiro.web;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.ExecutionException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            Subject subject = new TestSubject(subjectRoles);
            ThreadContext.bind(subject);

            try {
                AnyRoleAuthorizationFilter f = new AnyRoleAuthorizationFilter();
                return f.isAccessAllowed(null, null, filterRoles);
            } finally {
                ThreadContext.unbindSubject();
            }
        }

    }

    static class TestSubject implements Subject {

        final String[] roles;

        public TestSubject(String[] roles) {
            this.roles = roles;
        }

        @Override
        public boolean[] hasRoles(List<String> roleIdentifiers) {
            int slen = roles.length;
            boolean[] matches = new boolean[slen];
            for (int i = 0; i < slen; i++) {
                matches[i] = roleIdentifiers.contains(roles[i]);
            }

            return matches;
        }


        @Override
        public <V> Callable<V> associateWith(Callable<V> callable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getPrincipal() {
            throw new UnsupportedOperationException();
        }

        @Override
        public PrincipalCollection getPrincipals() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isPermitted(String permission) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isPermitted(Permission permission) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean[] isPermitted(String... permissions) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean[] isPermitted(List<Permission> permissions) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isPermittedAll(String... permissions) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isPermittedAll(Collection<Permission> permissions) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkPermission(String permission) throws AuthorizationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkPermission(Permission permission) throws AuthorizationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkPermissions(String... permissions) throws AuthorizationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkPermissions(Collection<Permission> permissions) throws AuthorizationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasRole(String roleIdentifier) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasAllRoles(Collection<String> roleIdentifiers) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkRole(String roleIdentifier) throws AuthorizationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkRoles(Collection<String> roleIdentifiers) throws AuthorizationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkRoles(String... roleIdentifiers) throws AuthorizationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void login(AuthenticationToken token) throws AuthenticationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isAuthenticated() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isRemembered() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Session getSession() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Session getSession(boolean create) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void logout() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <V> V execute(Callable<V> callable) throws ExecutionException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void execute(Runnable runnable) {
        }

        @Override
        public Runnable associateWith(Runnable runnable) {
            return null;
        }

        @Override
        public void runAs(PrincipalCollection principals) throws NullPointerException, IllegalStateException {

        }

        @Override
        public boolean isRunAs() {
            return false;
        }

        @Override
        public PrincipalCollection getPreviousPrincipals() {
            return null;
        }

        @Override
        public PrincipalCollection releaseRunAs() {
            return null;
        }
    }
}
