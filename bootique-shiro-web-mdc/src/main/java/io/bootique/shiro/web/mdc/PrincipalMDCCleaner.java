package io.bootique.shiro.web.mdc;

import io.bootique.shiro.mdc.PrincipalMDC;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

/**
 * @since 0.25
 */
public class PrincipalMDCCleaner implements ServletRequestListener {

    PrincipalMDC principalMDC;

    public PrincipalMDCCleaner(PrincipalMDC principalMDC) {
        this.principalMDC = principalMDC;
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        principalMDC.clear();
    }
}
