package org.keycloak.models.policy;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;

public class DisableUserActionProvider implements ResourceActionProvider {

    final KeycloakSession session;
    private final ComponentModel model;

    public DisableUserActionProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
    }

    @Override
    public void close() {

    }
}
