package org.keycloak.scim.resource.client;

import org.keycloak.scim.resource.ResourceTypeRepresentation;

public class OIDCClient extends ResourceTypeRepresentation {

    private String clientId;

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }
}
