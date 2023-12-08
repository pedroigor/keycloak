/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authorization.policy.provider.permission;

import java.util.HashSet;
import java.util.Set;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.Decision;
import org.keycloak.authorization.Decision.Effect;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.policy.evaluation.DefaultEvaluation;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.provider.PolicyProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public abstract class AbstractPermissionProvider implements PolicyProvider {

    @Override
    public void evaluate(Evaluation evaluation) {
        AuthorizationProvider authorization = evaluation.getAuthorizationProvider();
        DefaultEvaluation defaultEvaluation = DefaultEvaluation.class.cast(evaluation);
        Map<Policy, Map<Object, Decision.Effect>> decisionCache = defaultEvaluation.getDecisionCache();
        Policy policy = defaultEvaluation.getPolicy();
        ResourcePermission permission = evaluation.getPermission();

        Set<Evaluation> associatedEvaluations = new HashSet<>();
        for (Policy associatedPolicy : policy.getAssociatedPolicies()) {
            // create an evaluation specifically for the current policy with correct parent and base, copy collector, auth, cache, and context from parent
            DefaultEvaluation associatedEvaluation = new DefaultEvaluation(permission, defaultEvaluation.getContext(), policy, associatedPolicy,
                defaultEvaluation.getDecision(), authorization, decisionCache);

            // check if we have evaluated this policy for specifically the current permission being evaluated
            Map<Object, Decision.Effect> decisions = decisionCache.computeIfAbsent(associatedPolicy, p -> new HashMap<>());
            Decision.Effect effect = decisions.get(permission);
            if (effect == null) {
                PolicyProvider policyProvider = authorization.getProvider(associatedPolicy.getType());
                
                if (policyProvider == null) {
                    throw new RuntimeException("No policy provider found for policy [" + associatedPolicy.getType() + "]");
                }

                policyProvider.evaluate(associatedEvaluation);
                associatedEvaluation.denyIfNoEffect();
            } else {
                associatedEvaluation.setEffect(effect);
            }
            associatedEvaluations.add(associatedEvaluation);
        }

        switch (policy.getDecisionStrategy()) {
            case AFFIRMATIVE:
                if(associatedEvaluations.stream().anyMatch(eval -> Effect.PERMIT.equals(eval.getEffect()))) {
                    evaluation.grant();
                } else {
                    evaluation.deny();
                }
                break;
            case CONSENSUS:
                long count = associatedEvaluations.stream().filter(eval -> Effect.PERMIT.equals(eval.getEffect())).count();
                if(count >= associatedEvaluations.size() / 2) {
                    evaluation.grant();
                } else {
                    evaluation.deny();
                }
                break;
            default:
                // UNANIMOUS
                if(!associatedEvaluations.isEmpty() && associatedEvaluations.stream().allMatch(eval -> Effect.PERMIT.equals(eval.getEffect()))) {
                    evaluation.grant();
                } else {
                    evaluation.deny();
                }
        }
    }

    @Override
    public void close() {

    }
}
