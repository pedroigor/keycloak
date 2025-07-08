package org.keycloak.models.policy;

import org.keycloak.component.ComponentModel;

public class ResourceAction {

    private String id;
    private String providerId;

    public ResourceAction() {
        // reflection
    }

    public ResourceAction(String providerId) {
        this.providerId = providerId;
    }

    public ResourceAction(ComponentModel model) {
        this.id = model.getId();
        this.providerId = model.getProviderId();
    }

    public String getId() {
        return id;
    }

    public String getProviderId() {
        return providerId;
    }
}
