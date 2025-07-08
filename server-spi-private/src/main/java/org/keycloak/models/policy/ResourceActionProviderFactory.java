package org.keycloak.models.policy;

import org.keycloak.component.ComponentFactory;

public interface ResourceActionProviderFactory<P extends ResourceActionProvider> extends ComponentFactory<P, ResourceActionProvider> {

    ResourceType getType();
}
