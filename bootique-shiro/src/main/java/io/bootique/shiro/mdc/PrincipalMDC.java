package io.bootique.shiro.mdc;

import org.slf4j.MDC;

/**
 * A wrapper around SLF4J MDC (Mapped Diagnostic Context) class that allows to expose current principal name in the
 * logging context.
 *
 * @since 0.25
 */
public class PrincipalMDC {

    private static final String PRINCIPAL_MDC_KEY = "principal";

    /**
     * Initializes SLF4J MDC with the current principal name.
     */
    public void reset(Object principal) {
        if (principal == null) {
            MDC.remove(PRINCIPAL_MDC_KEY);
        } else {
            MDC.put(PRINCIPAL_MDC_KEY, String.valueOf(principal));
        }
    }

    /**
     * Removes principal name from the logging MDC.
     */
    public void clear() {
        MDC.remove(PRINCIPAL_MDC_KEY);
    }
}
