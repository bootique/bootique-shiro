package io.bootique.shiro.web;

import com.google.inject.Inject;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.test.junit.JettyTestFactory;
import io.bootique.shiro.ShiroModule;
import io.bootique.shiro.SubjectManager;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ShiroWebModuleIT {

    private static WebTarget BASE = ClientBuilder.newClient().target("http://localhost:8080/");

    @ClassRule
    public static JettyTestFactory TEST_FACTORY = new JettyTestFactory();

    @BeforeClass
    public static void beforeClass() {
        TEST_FACTORY.app("-c", "classpath:ShiroWebModuleIT.yml")
                .module(b -> JerseyModule.extend(b).addResource(Api.class))
                .module(b -> ShiroModule.extend(b).addRealm(new TestRealm()))
                // overriding standard "perms" filter to avoid being sent to the login form
                .module(b -> ShiroWebModule.extend(b).setFilter("perms", PermissionsFilter.class))
                .autoLoadModules().start();
    }

    @Test
    public void testPublic() {

        Response r1 = BASE.path("/public").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("public_string", r1.readEntity(String.class));
    }

    @Test
    public void testAnonymous() {
        Response r1 = BASE.path("/anonymous").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("anon_string_null", r1.readEntity(String.class));
    }

    @Test
    public void testLogin() {
        Response r1 = BASE.path("/login_on_demand").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("postlogin_string_myuser", r1.readEntity(String.class));
    }

    @Test
    public void testAdmin() {
        Response r1 = BASE.path("/admin").request().get();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), r1.getStatus());
    }

    @Path("/")
    public static class Api {

        @Inject
        private SubjectManager subjectManager;

        @GET
        @Path("public")
        public String getPublic() {
            return "public_string";
        }

        @GET
        @Path("anonymous")
        public String getAnonymous() {
            Subject subject = subjectManager.subject();
            return "anon_string_" + subject.getPrincipal();
        }

        @GET
        @Path("login_on_demand")
        public String getAdmin_Login() {
            Subject subject = subjectManager.subject();
            subject.login(new UsernamePasswordToken("myuser", "password"));
            subject.checkPermission("admin");

            return "postlogin_string_" + subject.getPrincipal();
        }

        @GET
        @Path("admin")
        public String getAdminNoLogin() {
            Subject subject = subjectManager.subject();
            throw new IllegalStateException("Should have been filtered: " + subject.getPrincipal());
        }
    }

    public static class TestRealm extends AuthorizingRealm {

        public TestRealm() {
            setName("TestRealm");
        }

        @Override
        public boolean supports(AuthenticationToken token) {
            return token instanceof UsernamePasswordToken;
        }

        @Override
        protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

            UsernamePasswordToken upToken = (UsernamePasswordToken) token;
            if (!"password".equals(new String(upToken.getPassword()))) {
                throw new AuthenticationException("Invalid password for user: " + upToken.getUsername());
            }

            return new SimpleAuthenticationInfo(upToken.getPrincipal(), upToken.getCredentials(), getName());
        }

        @Override
        protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
            info.addStringPermission("admin");
            return info;
        }
    }

    public static class PermissionsFilter extends PermissionsAuthorizationFilter {

        @Override
        protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
            WebUtils.toHttp(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
}
