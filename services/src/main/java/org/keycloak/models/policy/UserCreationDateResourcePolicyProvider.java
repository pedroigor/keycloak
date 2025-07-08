package org.keycloak.models.policy;

import org.keycloak.models.KeycloakSession;

public class UserCreationDateResourcePolicyProvider implements ResourcePolicyProvider {

    private KeycloakSession session;

    public UserCreationDateResourcePolicyProvider(KeycloakSession session) {
        session = session;
    }

    @Override
    public void close() {
        // no-op
    }
}
