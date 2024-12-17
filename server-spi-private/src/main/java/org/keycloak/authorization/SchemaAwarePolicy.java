/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.authorization;

import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.BadRequestException;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.common.Profile.Feature;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelValidationException;
import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.Logic;

public class SchemaAwarePolicy implements Policy {

    private Policy delegate;
    private final KeycloakSession session;

    public SchemaAwarePolicy(Policy policy, KeycloakSession session) {
        this.delegate = policy;
        this.session = session;
        checkIfSupportedPolicyType();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public String getType() {
        return delegate.getType();
    }

    @Override
    public DecisionStrategy getDecisionStrategy() {
        return delegate.getDecisionStrategy();
    }

    @Override
    public void setDecisionStrategy(DecisionStrategy decisionStrategy) {
        delegate.setDecisionStrategy(decisionStrategy);
    }

    @Override
    public Logic getLogic() {
        return delegate.getLogic();
    }

    @Override
    public void setLogic(Logic logic) {
        delegate.setLogic(logic);
    }

    @Override
    public Map<String, String> getConfig() {
        return delegate.getConfig();
    }

    @Override
    public void setConfig(Map<String, String> config) {
        delegate.setConfig(config);
    }

    @Override
    public void removeConfig(String name) {
        delegate.removeConfig(name);
    }

    @Override
    public void putConfig(String name, String value) {
        delegate.putConfig(name, value);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public void setName(String name) {
        delegate.setName(name);
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public void setDescription(String description) {
        delegate.setDescription(description);
    }

    @Override
    public ResourceServer getResourceServer() {
        return delegate.getResourceServer();
    }

    @Override
    public Set<Policy> getAssociatedPolicies() {
        return delegate.getAssociatedPolicies();
    }

    @Override
    public Set<Resource> getResources() {
        return delegate.getResources();
    }

    @Override
    public Set<Scope> getScopes() {
        return delegate.getScopes();
    }

    @Override
    public String getOwner() {
        return delegate.getOwner();
    }

    @Override
    public void setOwner(String owner) {
        delegate.setOwner(owner);
    }

    @Override
    public void addScope(Scope scope) {
        delegate.addScope(scope);
    }

    @Override
    public void removeScope(Scope scope) {
        delegate.removeScope(scope);
    }

    @Override
    public void addAssociatedPolicy(Policy associatedPolicy) {
        delegate.addAssociatedPolicy(associatedPolicy);
    }

    @Override
    public void removeAssociatedPolicy(Policy associatedPolicy) {
        delegate.removeAssociatedPolicy(associatedPolicy);
    }

    @Override
    public void addResource(Resource resource) {
        delegate.addResource(resource);
    }

    @Override
    public void removeResource(Resource resource) {
        delegate.removeResource(resource);
    }

    private void checkIfSupportedPolicyType() throws BadRequestException {
        if (AdminPermissionsAuthorizationSchema.INSTANCE.isSupportedPolicyType(session, getResourceServer(), getType())) {
            return;
        }

        throw new ModelValidationException("Policy type not supported by feature " + Feature.ADMIN_FINE_GRAINED_AUTHZ_V2.getVersionedKey());    }
}
