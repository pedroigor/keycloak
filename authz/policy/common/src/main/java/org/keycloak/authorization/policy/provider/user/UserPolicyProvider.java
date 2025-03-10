/*
 *  Copyright 2016 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.authorization.policy.provider.user;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jboss.logging.Logger;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.evaluation.EvaluationContext;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.ResourceType;
import org.keycloak.representations.idm.authorization.UserPolicyRepresentation;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class UserPolicyProvider implements PolicyProvider {

    private static final Logger logger = Logger.getLogger(UserPolicyProvider.class);

    private final BiFunction<Policy, AuthorizationProvider, UserPolicyRepresentation> representationFunction;

    public UserPolicyProvider(BiFunction<Policy, AuthorizationProvider, UserPolicyRepresentation> representationFunction) {
        this.representationFunction = representationFunction;
    }

    @Override
    public void evaluate(Evaluation evaluation) {
        EvaluationContext context = evaluation.getContext();
        UserPolicyRepresentation representation = representationFunction.apply(evaluation.getPolicy(), evaluation.getAuthorizationProvider());

        for (String userId : representation.getUsers()) {
            if (context.getIdentity().getId().equals(userId)) {
                evaluation.grant();
                break;
            }
        }
        logger.debugf("User policy %s evaluated to status %s on identity %s with accepted users: %s", evaluation.getPolicy().getName(), evaluation.getEffect(), evaluation.getContext().getIdentity().getId(), representation.getUsers());
    }

    @Override
    public void filter(KeycloakSession session, ResourceType resourceType, EntityManager em, CriteriaBuilder criteriaBuilder, Root<?> root, List<Predicate> predicates) {
        AuthorizationProvider provider = session.getProvider(AuthorizationProvider.class);
        RealmModel realm = session.getContext().getRealm();
        ClientModel adminPermissionsClient = realm.getAdminPermissionsClient();
        StoreFactory storeFactory = provider.getStoreFactory();
        ResourceServer resourceServer = storeFactory.getResourceServerStore().findByClient(adminPermissionsClient);
        UserModel adminUser = session.getContext().getUser();
        PolicyStore policyStore = storeFactory.getPolicyStore();

        List<UserPolicyRepresentation> policies = policyStore.findByType(resourceServer, UserPolicyProviderFactory.ID).stream()
                .map((p) -> {
                    UserPolicyRepresentation r = representationFunction.apply(p, provider);
                    r.setId(p.getId());
                    return r;
                })
                .filter(r -> r.getUsers().contains(adminUser.getId())).toList();

        if (policies.isEmpty()) {
            return;
        }

        Set<String> resourceIds = new HashSet<>();

        for (UserPolicyRepresentation policy : policies) {
            resourceIds.addAll(policyStore.findDependentPolicies(resourceServer, policy.getId()).stream()
                    .filter((permission) -> {
                        return resourceType.getType().equals(permission.getResourceType());
                    })
                    .flatMap((Function<Policy, Stream<Resource>>) policy1 -> policy1.getResources().stream()).map(Resource::getName).toList());
        }

        if (resourceIds.isEmpty()) {
            return;
        }

        predicates.add(root.get("id").in(resourceIds));
    }

    @Override
    public void close() {

    }
}
