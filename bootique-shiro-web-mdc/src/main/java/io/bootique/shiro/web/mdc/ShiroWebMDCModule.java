package io.bootique.shiro.web.mdc;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.jetty.JettyModule;
import io.bootique.shiro.ShiroModule;
import io.bootique.shiro.mdc.PrincipalMDC;

/**
 * @since 0.25
 */
public class ShiroWebMDCModule implements Module {

    @Override
    public void configure(Binder binder) {
        JettyModule.extend(binder).addListener(PrincipalMDCCleaner.class);
        ShiroModule.extend(binder).addAuthListener(PrincipalMDCInitializer.class);
    }

    @Singleton
    @Provides
    PrincipalMDCCleaner providePrincipalMDCCleaner(PrincipalMDC principalMDC) {
        return new PrincipalMDCCleaner(principalMDC);
    }

    @Singleton
    @Provides
    PrincipalMDCInitializer providePrincipalMDCInitializer(PrincipalMDC principalMDC) {
        return new PrincipalMDCInitializer(principalMDC);
    }
}
