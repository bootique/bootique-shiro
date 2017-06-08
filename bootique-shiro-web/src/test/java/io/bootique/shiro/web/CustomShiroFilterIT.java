package io.bootique.shiro.web;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import io.bootique.BQRuntime;
import io.bootique.jetty.MappedFilter;
import io.bootique.jetty.test.junit.JettyTestFactory;
import io.bootique.shiro.ShiroModule;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import static org.mockito.Mockito.mock;


public class CustomShiroFilterIT {

    @ClassRule
    public static JettyTestFactory TEST_FACTORY = new JettyTestFactory();

    @Test
    public void testCustomFactory() {
        BQRuntime runtime = TEST_FACTORY
                .app("-c", "classpath:CustomShiroFilterIT.yml")
                .autoLoadModules()
                .module(b -> ShiroModule.extend(b).addRealm(mock(Realm.class)))
                .createRuntime();

        TypeLiteral<MappedFilter<ShiroFilter>> filterKey = new TypeLiteral<MappedFilter<ShiroFilter>>() {
        };
        MappedFilter<ShiroFilter> mappedFilter = runtime.getInstance(Key.get(filterKey));

        Assert.assertTrue(mappedFilter.getFilter() instanceof CustomFilter);
    }

    @JsonTypeName("it")
    public static class CustomFactory extends MappedShiroFilterFactory {
        @Override
        protected ShiroFilter createShiroFilter(WebSecurityManager securityManager, FilterChainResolver chainResolver) {
            return new CustomFilter(securityManager, chainResolver);
        }
    }

    public static class CustomFilter extends ShiroFilter {
        public CustomFilter(WebSecurityManager securityManager, FilterChainResolver filterChainResolver) {
            super(securityManager, filterChainResolver);
        }
    }
}
