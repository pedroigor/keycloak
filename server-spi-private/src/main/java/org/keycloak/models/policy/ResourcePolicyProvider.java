package org.keycloak.models.policy;

import java.util.List;

import org.keycloak.provider.Provider;

public interface ResourcePolicyProvider extends Provider {

    List<Object> getResources();
}
