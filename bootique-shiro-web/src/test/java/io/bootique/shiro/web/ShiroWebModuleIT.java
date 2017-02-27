package io.bootique.shiro.web;

import com.google.inject.Inject;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.test.junit.JettyTestFactory;
import io.bootique.shiro.SubjectManager;
import org.apache.shiro.subject.Subject;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class ShiroWebModuleIT {

    private static WebTarget BASE = ClientBuilder.newClient().target("http://localhost:8080/");

    @ClassRule
    public static JettyTestFactory TEST_FACTORY = new JettyTestFactory();

    @BeforeClass
    public static void beforeClass() {
        TEST_FACTORY.app()
                .module(b -> JerseyModule.extend(b).addResource(Api.class))
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
        assertEquals("anon_string_principal", r1.readEntity(String.class));
    }

    @Test
    public void testPrivate() {
        Response r2 = BASE.path("/admin").request().get();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), r2.getStatus());
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
        @Path("admin")
        public String getAdmin() {
            Subject subject = subjectManager.subject();

            subject.checkPermission("admin");

            return "admin_string_" + subject.getPrincipal();
        }
    }
}
