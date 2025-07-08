package org.keycloak.models.policy;

import org.keycloak.component.ComponentModel;

public class ResourcePolicy {

    private String providerId;
    private String id;

    public ResourcePolicy() {
        // reflection
    }

    public ResourcePolicy(String providerId) {
        this.providerId = providerId;
        this.id = null;
    }

    public ResourcePolicy(ComponentModel c) {
        this.id = c.getId();
        this.providerId = c.getProviderId();
    }

    public String getId() {
        return id;
    }

    public String getProviderId() {
        return providerId;
    }
}
