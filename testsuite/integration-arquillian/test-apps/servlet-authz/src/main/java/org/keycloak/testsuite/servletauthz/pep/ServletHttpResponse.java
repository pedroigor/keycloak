package org.keycloak.testsuite.servletauthz.pep;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

import org.keycloak.adapters.authorization.spi.HttpResponse;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ServletHttpResponse implements HttpResponse {

    private HttpServletResponse response;

    public ServletHttpResponse(HttpServletResponse response) {
        this.response = response;
    }

    @Override
    public void sendError(int status) {
        try {
            response.sendError(status);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendError(int status, String reason) {
        try {
            response.sendError(status, reason);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setHeader(String name, String value) {
        response.setHeader(name, value);
    }
}
