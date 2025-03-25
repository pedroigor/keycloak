/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authorization.admin.representation;

import org.keycloak.authorization.AdminPermissionsSchema;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.Decision;
import org.keycloak.authorization.Decision.Effect;
import org.keycloak.authorization.admin.PolicyEvaluationService.EvaluationDecisionCollector;
import org.keycloak.authorization.common.KeycloakIdentity;
import org.keycloak.authorization.model.PermissionTicket;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.policy.evaluation.Result;
import org.keycloak.authorization.policy.evaluation.Result.PolicyResult;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.authorization.DecisionEffect;
import org.keycloak.representations.idm.authorization.PolicyEvaluationRequest;
import org.keycloak.representations.idm.authorization.PolicyEvaluationResponse;
import org.keycloak.representations.idm.authorization.PolicyEvaluationResponse.PolicyResultRepresentation;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PolicyEvaluationResponseBuilder {
    public static PolicyEvaluationResponse build(EvaluationDecisionCollector decision, ResourceServer resourceServer, AuthorizationProvider authorization, KeycloakIdentity identity, PolicyEvaluationRequest request) {
        PolicyEvaluationResponse response = new PolicyEvaluationResponse();
        List<PolicyEvaluationResponse.EvaluationResultRepresentation> resultsRep = new ArrayList<>();
        AccessToken accessToken = identity.getAccessToken();
        AccessToken.Authorization authorizationData = new AccessToken.Authorization();

        authorizationData.setPermissions(decision.results());
        accessToken.setAuthorization(authorizationData);

        ClientModel clientModel = authorization.getRealm().getClientById(resourceServer.getClientId());

        if (!accessToken.hasAudience(clientModel.getClientId())) {
            accessToken.audience(clientModel.getClientId());
        }

        response.setRpt(accessToken);

        Collection<Result> results = decision.getResults();

        if (results.stream().flatMap(result -> result.getResults().stream()).allMatch(evaluationResult -> evaluationResult.getEffect().equals(Effect.PERMIT))) {
            response.setStatus(DecisionEffect.PERMIT);
        } else {
            response.setStatus(DecisionEffect.DENY);
        }

        for (Result result : results) {
            PolicyEvaluationResponse.EvaluationResultRepresentation rep = new PolicyEvaluationResponse.EvaluationResultRepresentation();

            if (result.getEffect() == Decision.Effect.DENY) {
                rep.setStatus(DecisionEffect.DENY);
            } else {
                rep.setStatus(DecisionEffect.PERMIT);

            }

            Resource resource = result.getPermission().getResource();

            if (resource != null) {
                if (resource.getName().equals(request.getResourceType()) && !results.stream().map(Result::getPermission)
                        .map(ResourcePermission::getResource)
                        .map(Resource::getName)
                        .allMatch(request.getResourceType()::equals)) {
                    continue;
                }

                ResourceRepresentation resourceRep = new ResourceRepresentation();

                resourceRep.setId(resource.getId());
                resourceRep.setName(resource.getName());

                rep.setResource(resourceRep);
            } else {
                ResourceRepresentation resourceRep = new ResourceRepresentation();

                resourceRep.setName("Any Resource with Scopes " + result.getPermission().getScopes().stream().map(Scope::getName).toList());

                rep.setResource(resourceRep);
            }

            resultsRep.add(rep);

            rep.setScopes(result.getPermission().getScopes().stream().map(scope -> {
                ScopeRepresentation representation = new ScopeRepresentation();

                representation.setId(scope.getId());
                representation.setName(scope.getName());

                return representation;
            }).collect(Collectors.toList()));

            Set<PolicyEvaluationResponse.PolicyResultRepresentation> policies = new HashSet<>();

            for (Result.PolicyResult policy : result.getResults()) {
                PolicyResultRepresentation policyRep = toRepresentation(policy, authorization);

                if ("resource".equals(policy.getPolicy().getType())) {
                    policyRep.getPolicy().setScopes(resource.getScopes().stream().map(Scope::getName).collect(Collectors.toSet()));
                }

                if (Effect.PERMIT.equals(policy.getEffect())) {
                    rep.getAllowedScopes().addAll(policy.getPolicy().getScopes().stream().map(ModelToRepresentation::toRepresentation).toList());
                } else {
                    rep.getDeniedScopes().addAll(policy.getPolicy().getScopes().stream().map(ModelToRepresentation::toRepresentation).toList());
                }

                policyRep.setResourceType(policy.getPolicy().getResourceType());

                policies.add(policyRep);
            }

            rep.setPolicies(policies);
        }

        resultsRep.sort(Comparator.comparing(o -> o.getResource().getName()));

        Map<String, PolicyEvaluationResponse.EvaluationResultRepresentation> groupedResults = new HashMap<>();

        resultsRep.forEach(evaluationResultRepresentation -> {
            PolicyEvaluationResponse.EvaluationResultRepresentation result = groupedResults.get(evaluationResultRepresentation.getResource().getId());
            ResourceRepresentation resource = evaluationResultRepresentation.getResource();

            if (result == null) {
                groupedResults.put(resource.getId(), evaluationResultRepresentation);
                result = evaluationResultRepresentation;
            }

            if (result.getStatus().equals(DecisionEffect.PERMIT) || (evaluationResultRepresentation.getStatus().equals(DecisionEffect.PERMIT) && result.getStatus().equals(DecisionEffect.DENY))) {
                result.setStatus(DecisionEffect.PERMIT);
            }

            List<ScopeRepresentation> scopes = new ArrayList<>(result.getScopes());

            if (DecisionEffect.PERMIT.equals(result.getStatus())) {
                result.getAllowedScopes().addAll(scopes);
            } else {
                result.getDeniedScopes().addAll(scopes);
            }

            if (AdminPermissionsSchema.SCHEMA.isAdminPermissionsEnabled(authorization.getRealm())) {
                for (Result rt : results) {
                    Resource rs = rt.getPermission().getResource();

                    if (rs.getName().equals(request.getResourceType())) {
                        for (PolicyResult rtResult : rt.getResults()) {
                            Policy policy = rtResult.getPolicy();

                            if (policy.getScopes().stream().map(Scope::getName).noneMatch(AdminPermissionsSchema.VIEW::equals)) {
                                result.getPolicies().add(toRepresentation(rtResult, authorization));
                                break;
                            }

                            result.getPolicies().add(toRepresentation(rtResult, authorization));

                            ScopeRepresentation s = rs.getScopes().stream().map(ModelToRepresentation::toRepresentation).filter((s1) -> s1.getName().equals(AdminPermissionsSchema.VIEW)).findAny().orElse(null);

                            if (s == null) {
                                break;
                            }

                            if (rtResult.getEffect().equals(Effect.PERMIT)) {
                                result.getAllowedScopes().add(s);
                                result.getDeniedScopes().remove(s);
                            } else {
                                result.getDeniedScopes().add(s);
                                result.getAllowedScopes().remove(s);
                            }
                        }
                    }
                }
            }

            result.getAllowedScopes().removeAll(result.getDeniedScopes());

            if (resource.getId() != null) {
                String resourceType = result.getPolicies().stream().map(PolicyResultRepresentation::getResourceType).filter(Objects::nonNull).findAny().orElse(null);
                String resourceName = AdminPermissionsSchema.SCHEMA.getResourceName(authorization.getKeycloakSession(), resourceServer, resourceType, evaluationResultRepresentation.getResource().getName());

                if (!scopes.isEmpty()) {
                    result.getResource().setName(resourceName + " with scopes " + scopes.stream().flatMap((Function<ScopeRepresentation, Stream<?>>) scopeRepresentation -> Arrays.asList(scopeRepresentation.getName()).stream()).collect(Collectors.toList()));
                } else {
                    result.getResource().setName(resourceName);
                }
            } else {
                result.getResource().setName("Any Resource with Scopes " + scopes.stream().flatMap((Function<ScopeRepresentation, Stream<?>>) scopeRepresentation -> Arrays.asList(scopeRepresentation.getName()).stream()).collect(Collectors.toList()));
            }

            Resource r = authorization.getStoreFactory().getResourceStore().findById(resourceServer, resource.getId());

            result.getDeniedScopes().addAll(r.getScopes().stream().map(ModelToRepresentation::toRepresentation).filter(Predicate.not(result.getAllowedScopes()::contains)).toList());

            List<ScopeRepresentation> requestedScopes = request.getResources().stream().filter(resourceRepresentation -> resourceRepresentation.getName() != null && r.getName().equals(resourceRepresentation.getName())).flatMap((Function<ResourceRepresentation, Stream<ScopeRepresentation>>) resourceRepresentation -> resourceRepresentation.getScopes().stream()).toList();

            if (requestedScopes.isEmpty()) {
                requestedScopes = scopes;
            }

            if (!requestedScopes.isEmpty()) {
                if (requestedScopes.stream().anyMatch(result.getAllowedScopes()::contains)) {
                    response.setStatus(DecisionEffect.PERMIT);
                } else {
                    response.setStatus(DecisionEffect.DENY);
                }
            }

            result.getPolicies().addAll(evaluationResultRepresentation.getPolicies());
        });

        response.setResults(new ArrayList<>(groupedResults.values()));

        return response;
    }

    private static PolicyEvaluationResponse.PolicyResultRepresentation toRepresentation(Result.PolicyResult result, AuthorizationProvider authorization) {
        PolicyEvaluationResponse.PolicyResultRepresentation policyResultRep = new PolicyEvaluationResponse.PolicyResultRepresentation();

        PolicyRepresentation representation = new PolicyRepresentation();
        Policy policy = result.getPolicy();
        ResourceServer resourceServer = policy.getResourceServer();

        representation.setId(policy.getId());
        representation.setName(policy.getName());
        representation.setType(policy.getType());
        representation.setDecisionStrategy(policy.getDecisionStrategy());
        representation.setDescription(policy.getDescription());

        if ("uma".equals(representation.getType())) {
            Map<PermissionTicket.FilterOption, String> filters = new EnumMap<>(PermissionTicket.FilterOption.class);

            filters.put(PermissionTicket.FilterOption.POLICY_ID, policy.getId());

            List<PermissionTicket> tickets = authorization.getStoreFactory().getPermissionTicketStore().find(resourceServer, filters, -1, 1);

            if (!tickets.isEmpty()) {
                KeycloakSession keycloakSession = authorization.getKeycloakSession();
                RealmModel realm = authorization.getRealm();
                PermissionTicket ticket = tickets.get(0);
                UserModel userOwner = keycloakSession.users().getUserById(realm, ticket.getOwner());
                UserModel requester = keycloakSession.users().getUserById(realm, ticket.getRequester());
                String resourceOwner;
                if (userOwner != null) {
                    resourceOwner = getUserEmailOrUserName(userOwner);
                } else {
                    ClientModel clientOwner = realm.getClientById(ticket.getOwner());
                    resourceOwner = clientOwner.getClientId();
                }

                representation.setDescription("Resource owner (" + resourceOwner + ") grants access to " + getUserEmailOrUserName(requester));
            } else {
                String description = representation.getDescription();

                if (description != null) {
                    representation.setDescription(description + " (User-Managed Policy)");
                } else {
                    representation.setDescription("User-Managed Policy");
                }
            }
        }

        representation.setResources(policy.getResources().stream().map(resource -> resource.getName()).collect(Collectors.toSet()));

        Set<String> scopeNames = policy.getScopes().stream().map(scope -> scope.getName()).collect(Collectors.toSet());

        representation.setScopes(scopeNames);

        policyResultRep.setPolicy(representation);

        if (result.getEffect() == Decision.Effect.DENY) {
            policyResultRep.setStatus(DecisionEffect.DENY);
            policyResultRep.setScopes(representation.getScopes());
        } else {
            policyResultRep.setStatus(DecisionEffect.PERMIT);
        }

        policyResultRep.setAssociatedPolicies(result.getAssociatedPolicies().stream().map(policy1 -> toRepresentation(policy1, authorization)).collect(Collectors.toList()));

        return policyResultRep;
    }

    private static String getUserEmailOrUserName(UserModel user) {
        return (user.getEmail() != null ? user.getEmail() : user.getUsername());
    }
}
