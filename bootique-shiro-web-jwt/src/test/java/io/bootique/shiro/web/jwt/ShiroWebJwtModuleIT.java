package io.bootique.shiro.web.jwt;

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.junit5.JettyTester;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

public abstract class ShiroWebJwtModuleIT {

    protected static BQRuntime runtime(JettyTester jetty, String yml) {
        return Bootique
                .app("-c", yml, "-s")
                .module(jetty.moduleReplacingConnectors())
                .module(b -> JerseyModule.extend(b).addResource(TestApi.class))
                .autoLoadModules()
                .createRuntime();
    }

    @Path("/")
    public static class TestApi {

        @GET
        @Path("public")
        public String getPublic() {
            return "public";
        }

        @GET
        @Path("private-one")
        public String getPrivateOne() {
            return "private-one";
        }

        @GET
        @Path("private-two")
        public String getPrivateTwo() {
            return "private-two";
        }

        @GET
        @Path("private-three")
        public String getPrivateThree() {
            return "private-three";
        }
    }
}
