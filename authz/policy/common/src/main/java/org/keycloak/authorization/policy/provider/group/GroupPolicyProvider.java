/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authorization.policy.provider.group;

import static org.keycloak.models.utils.ModelToRepresentation.buildGroupPath;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jboss.logging.Logger;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.attribute.Attributes;
import org.keycloak.authorization.attribute.Attributes.Entry;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.provider.PartialEvaluationPolicyProvider;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.authorization.GroupPolicyRepresentation;
import org.keycloak.representations.idm.authorization.ResourceType;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class GroupPolicyProvider implements PolicyProvider, PartialEvaluationPolicyProvider {

    private static final Logger logger = Logger.getLogger(GroupPolicyProvider.class);
    private final BiFunction<Policy, AuthorizationProvider, GroupPolicyRepresentation> representationFunction;

    public GroupPolicyProvider(BiFunction<Policy, AuthorizationProvider, GroupPolicyRepresentation> representationFunction) {
        this.representationFunction = representationFunction;
    }

    @Override
    public void evaluate(Evaluation evaluation) {
        AuthorizationProvider authorizationProvider = evaluation.getAuthorizationProvider();
        GroupPolicyRepresentation policy = representationFunction.apply(evaluation.getPolicy(), authorizationProvider);
        RealmModel realm = authorizationProvider.getRealm();
        Attributes.Entry groupsClaim = evaluation.getContext().getIdentity().getAttributes().getValue(policy.getGroupsClaim());

        if (groupsClaim == null || groupsClaim.isEmpty()) {
            List<String> userGroups = evaluation.getRealm().getUserGroups(evaluation.getContext().getIdentity().getId());
            groupsClaim = new Entry(policy.getGroupsClaim(), userGroups);
        }

        for (GroupPolicyRepresentation.GroupDefinition definition : policy.getGroups()) {
            GroupModel allowedGroup = realm.getGroupById(definition.getId());

            if (allowedGroup == null) {
                continue;
            }

            for (int i = 0; i < groupsClaim.size(); i++) {
                String group = groupsClaim.asString(i);

                if (group.indexOf('/') != -1) {
                    String allowedGroupPath = buildGroupPath(allowedGroup);
                    if (group.equals(allowedGroupPath) || (definition.isExtendChildren() && group.startsWith(allowedGroupPath))) {
                        evaluation.grant();
                        return;
                    }
                }

                // in case the group from the claim does not represent a path, we just check an exact name match
                if (group.equals(allowedGroup.getName())) {
                    evaluation.grant();
                    return;
                }
            }
        }
        logger.debugf("Groups policy %s evaluated to %s with identity groups %s", policy.getName(), evaluation.getEffect(), groupsClaim);
    }

    @Override
    public List<Policy> getPermissions(KeycloakSession session, UserModel user, ResourceType resourceType) {
        AuthorizationProvider provider = session.getProvider(AuthorizationProvider.class);
        RealmModel realm = session.getContext().getRealm();
        ClientModel adminPermissionsClient = realm.getAdminPermissionsClient();
        StoreFactory storeFactory = provider.getStoreFactory();
        ResourceServer resourceServer = storeFactory.getResourceServerStore().findByClient(adminPermissionsClient);
        PolicyStore policyStore = storeFactory.getPolicyStore();
        List<GroupPolicyRepresentation> policies = policyStore.findByType(resourceServer, GroupPolicyProviderFactory.ID).stream()
                .map((p) -> {
                    GroupPolicyRepresentation r = representationFunction.apply(p, provider);
                    r.setLogic(p.getLogic());
                    r.setId(p.getId());
                    return r;
                })
                .filter(r -> r.getGroups().stream().anyMatch((definition) -> {
                    GroupModel group = realm.getGroupById(definition.getId());
                    return user.isMemberOf(group);
                })).toList();

        return policies.stream()
                .flatMap((Function<GroupPolicyRepresentation, Stream<Policy>>) policy -> policyStore.findDependentPolicies(resourceServer, policy.getId()).stream()
                .filter((permission) -> resourceType.getType().equals(permission.getResourceType()))).toList();
    }

    @Override
    public void close() {

    }
}
