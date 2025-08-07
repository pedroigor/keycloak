/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.tests.admin.model.policy;

import java.util.List;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.policy.ResourceAction;
import org.keycloak.models.policy.ResourcePolicy;
import org.keycloak.models.policy.ResourcePolicyManager;

public class PolicyBuilder {

    public static PolicyBuilder of(String policyId) {
        return new PolicyBuilder(policyId);
    }


    private final String providerId;
    private ResourceAction[] actions;

    public PolicyBuilder(String providerId) {
        this.providerId = providerId;
    }

    public PolicyBuilder withActions(ResourceAction... actions) {
        this.actions = actions;
        return this;
    }

    public ResourcePolicyManager build(KeycloakSession session) {
        ResourcePolicyManager manager = new ResourcePolicyManager(session);

        ResourcePolicy policy = manager.addPolicy(providerId);
        manager.updateActions(policy, List.of(actions));

        return manager;
    }
}
