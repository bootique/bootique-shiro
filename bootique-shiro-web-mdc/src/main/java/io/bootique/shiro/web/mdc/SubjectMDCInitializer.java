package io.bootique.shiro.web.mdc;

import io.bootique.shiro.mdc.PrincipalMDC;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * A Shiro filter that initializes MDC state from the current Subject. It needs to be explicitly placed in the
 * authentication chain in the "shiroweb.urls" configuration under the name "mdc". This filter is optional and is only
 * needed in session-based apps that do not perform login on every request.
 *
 * @since 0.25
 */
public class SubjectMDCInitializer implements Filter {

    private PrincipalMDC principalMDC;

    public SubjectMDCInitializer(PrincipalMDC principalMDC) {
        this.principalMDC = principalMDC;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        initMDC();
        chain.doFilter(request, response);
    }

    protected void initMDC() {
        Subject subject = SecurityUtils.getSubject();
        Object principal = subject.getPrincipals().getPrimaryPrincipal();
        principalMDC.reset(principal);
    }
}
