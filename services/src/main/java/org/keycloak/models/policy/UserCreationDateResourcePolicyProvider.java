package org.keycloak.models.policy;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;

public class UserCreationDateResourcePolicyProvider implements ResourcePolicyProvider {

    private final ComponentModel model;
    private KeycloakSession session;

    public UserCreationDateResourcePolicyProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public List<Object> getResources() {
        return List.of();
    }
}
