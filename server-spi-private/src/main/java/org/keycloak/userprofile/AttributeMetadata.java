/*
 *
 *  * Copyright 2021  Red Hat, Inc. and/or its affiliates
 *  * and other contributors as indicated by the @author tags.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.keycloak.userprofile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.keycloak.models.ClientScopeProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public final class AttributeMetadata {

    public static final Predicate<AttributeContext> ALWAYS_SELECT = context -> true;

    private final String attributeName;
    private final Predicate<AttributeContext> selector;
    private final boolean readOnly;
    private final boolean optional;
    private List<AttributeValidatorMetadata> validators;

    AttributeMetadata(String attributeName) {
        this(attributeName, ALWAYS_SELECT, false, false);
    }

    AttributeMetadata(String attributeName, boolean readOnly) {
        this(attributeName, ALWAYS_SELECT, readOnly, false);
    }

    AttributeMetadata(String attributeName, boolean readOnly, boolean optional) {
        this(attributeName, ALWAYS_SELECT, readOnly, optional);
    }

    AttributeMetadata(String attributeName, Predicate<AttributeContext> selector) {
        this(attributeName, selector, false, false);
    }

    AttributeMetadata(String attributeName, List<String> scopes) {
        this(attributeName, context -> {
            KeycloakSession session = context.getSession();
            AuthenticationSessionModel authSession = session.getContext().getAuthenticationSession();

            if (authSession == null) {
                return false;
            }

            ClientScopeProvider clientScopes = session.clientScopes();
            RealmModel realm = session.getContext().getRealm();

            if (authSession.getClientScopes().stream().anyMatch(scopes::contains)) {
                return true;
            }

            return authSession.getClientScopes().stream()
                    .map(id -> clientScopes.getClientScopeById(realm, id).getName())
                    .anyMatch(scopes::contains);
        });
    }

    AttributeMetadata(String attributeName, Predicate<AttributeContext> selector, boolean readOnly, boolean optional) {
        this.attributeName = attributeName;
        this.selector = selector;
        this.readOnly = readOnly;
        this.optional = optional;
    }

    public String getName() {
        return attributeName;
    }

    public boolean isSelected(AttributeContext context) {
        return selector.test(context);
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isOptional() {
        return optional;
    }

    List<AttributeValidatorMetadata> getValidators() {
        return validators;
    }

    AttributeMetadata addValidator(List<AttributeValidatorMetadata> validators) {
        if (this.validators == null) {
            this.validators = new ArrayList<>();
        }

        this.validators.addAll(validators.stream().filter(Objects::nonNull).collect(Collectors.toList()));

        return this;
    }

    AttributeMetadata addValidator(AttributeValidatorMetadata validator) {
        addValidator(Arrays.asList(validator));
        return this;
    }
}
