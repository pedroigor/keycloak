package org.keycloak.testsuite.servletauthz.pep;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.keycloak.adapters.authorization.TokenPrincipal;
import org.keycloak.adapters.authorization.spi.HttpRequest;
import org.wildfly.security.http.oidc.RefreshableOidcSecurityContext;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ServletHttpRequest implements HttpRequest {

    private final HttpServletRequest request;
    private final RefreshableOidcSecurityContext securityContext;
    private final TokenPrincipal tokenPrincipal;

    public ServletHttpRequest(HttpServletRequest request, RefreshableOidcSecurityContext securityContext) {
        this.request = request;
        this.securityContext = securityContext;
        this.tokenPrincipal = new TokenPrincipal() {
            @Override
            public String getRawToken() {
                return securityContext.getTokenString();
            }
        };
    }

    @Override
    public String getRelativePath() {
        return request.getServletPath();
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public String getURI() {
        return request.getRequestURI();
    }

    @Override
    public List<String> getHeaders(String name) {
        return Collections.list(request.getHeaders(name));
    }

    @Override
    public String getFirstParam(String name) {
        Map<String, String[]> parameters = request.getParameterMap();
        String[] values = parameters.get(name);

        if (values == null || values.length == 0) {
            return null;
        }

        return values[0];
    }

    @Override
    public String getCookieValue(String name) {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public InputStream getInputStream(boolean buffered) {
        return null;
    }

    @Override
    public TokenPrincipal getPrincipal() {
        return tokenPrincipal;
    }
}
