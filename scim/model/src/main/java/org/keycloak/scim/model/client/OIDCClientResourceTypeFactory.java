package org.keycloak.scim.model.client;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.scim.resource.spi.ScimResourceTypeProviderFactory;

public class OIDCClientResourceTypeFactory implements ScimResourceTypeProviderFactory<OIDCClientResourceType> {

    @Override
    public OIDCClientResourceType create(KeycloakSession session) {
        return new OIDCClientResourceType(session);
    }

    @Override
    public void init(Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "OIDCClients";
    }
}
