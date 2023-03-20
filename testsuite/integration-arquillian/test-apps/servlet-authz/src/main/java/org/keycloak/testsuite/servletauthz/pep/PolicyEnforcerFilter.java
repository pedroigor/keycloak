package org.keycloak.testsuite.servletauthz.pep;

import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.keycloak.AuthorizationContext;
import org.keycloak.adapters.authorization.PolicyEnforcer;
import org.wildfly.security.http.oidc.OidcClientConfiguration;
import org.wildfly.security.http.oidc.OidcPrincipal;
import org.wildfly.security.http.oidc.RefreshableOidcSecurityContext;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class PolicyEnforcerFilter implements Filter {

    private PolicyEnforcer policyEnforcer;

    @Override
    public void init(FilterConfig filterConfig) {
        // no-init
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpSession session = request.getSession(false);

        if (session == null) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        RefreshableOidcSecurityContext securityContext = (RefreshableOidcSecurityContext) ((OidcPrincipal) request.getUserPrincipal()).getOidcSecurityContext();
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        PolicyEnforcer policyEnforcer = getOrCreatePolicyEnforcer(request, securityContext);
        AuthorizationContext authzContext = policyEnforcer.enforce(new ServletHttpRequest(request, securityContext), new ServletHttpResponse(response));

        if (authzContext.isGranted()) {
            request.setAttribute(AuthorizationContext.class.getName(), authzContext);
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private synchronized PolicyEnforcer getOrCreatePolicyEnforcer(HttpServletRequest request, RefreshableOidcSecurityContext securityContext) {
        if (policyEnforcer == null) {
            InputStream enforcerConfig = request.getServletContext().getResourceAsStream("WEB-INF/policy-enforcer.json");
            OidcClientConfiguration configuration = securityContext.getOidcClientConfiguration();
            String authServerUrl = configuration.getProviderUrl().substring(0, configuration.getProviderUrl().indexOf("/realms"));
            return policyEnforcer = PolicyEnforcer.builder()
                    .authServerUrl(authServerUrl)
                    .realm(configuration.getRealm())
                    .clientId(configuration.getResource())
                    .credentials(configuration.getResourceCredentials())
                    .bearerOnly(false)
                    .enforcerConfig(enforcerConfig)
                    .httpClient(configuration.getClient()).build();
        }

        return policyEnforcer;
    }

    @Override
    public void destroy() {
    }
}
