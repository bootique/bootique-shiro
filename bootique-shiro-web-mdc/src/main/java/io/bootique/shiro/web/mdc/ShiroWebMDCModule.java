package io.bootique.shiro.web.mdc;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.jetty.JettyModule;
import io.bootique.shiro.ShiroModule;
import io.bootique.shiro.mdc.PrincipalMDC;
import io.bootique.shiro.web.ShiroWebModule;

/**
 * @since 0.25
 */
public class ShiroWebMDCModule implements Module {

    @Override
    public void configure(Binder binder) {
        JettyModule.extend(binder).addListener(MDCCleaner.class);
        ShiroModule.extend(binder).addAuthListener(OnAuthMDCInitializer.class);
        ShiroWebModule.extend(binder).setFilter("mdc", SubjectMDCInitializer.class);
    }

    @Singleton
    @Provides
    MDCCleaner providePrincipalMDCCleaner(PrincipalMDC principalMDC) {
        return new MDCCleaner(principalMDC);
    }

    @Singleton
    @Provides
    OnAuthMDCInitializer providePrincipalMDCInitializer(PrincipalMDC principalMDC) {
        return new OnAuthMDCInitializer(principalMDC);
    }

    @Singleton
    @Provides
    SubjectMDCInitializer provideSubjectMDCInitializer(PrincipalMDC principalMDC) {
        return new SubjectMDCInitializer(principalMDC);
    }
}
