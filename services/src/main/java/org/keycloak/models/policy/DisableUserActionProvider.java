package org.keycloak.models.policy;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.policy.ResourceAction;
import org.keycloak.models.policy.ResourceActionProvider;

public class DisableUserActionProvider implements ResourceActionProvider {

    final KeycloakSession session;

    public DisableUserActionProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public ResourceAction create() {
        return null;
    }

    @Override
    public void update(ResourceAction action) {

    }

    @Override
    public boolean delete(ResourceAction action) {
        return false;
    }

    @Override
    public ResourceAction findById(String id) {
        return null;
    }
}
