package io.bootique.shiro.web.mdc;

import io.bootique.shiro.mdc.PrincipalMDC;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

/**
 * Resets principal MDC information at the end of the web request.
 *
 * @since 0.25
 */
public class ShiroWebMDCCleaner implements ServletRequestListener {

    PrincipalMDC principalMDC;

    public ShiroWebMDCCleaner(PrincipalMDC principalMDC) {
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
