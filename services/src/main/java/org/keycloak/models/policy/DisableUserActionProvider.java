package org.keycloak.models.policy;

import java.time.Duration;
import java.util.Date;
import java.util.List;

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

    @Override
    public void run(List<Object> resources) {
        for (Object resource : resources) {

        }
    }

    @Override
    public boolean isRunnable() {
        return model.get("time") == null;
    }
}
