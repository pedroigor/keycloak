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

package org.keycloak.userprofile.legacy;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.validation.Validation;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.AttributeValidatorMetadata;
import org.keycloak.userprofile.validation.Validator;

import java.util.List;
import java.util.Map;

/**
 * Functions are supposed to return:
 * - true if validation success
 * - false if validation fails
 *
 * @author <a href="mailto:markus.till@bosch.io">Markus Till</a>
 */
public class Validators {

    public static final AttributeValidatorMetadata create(String message, Validator validator) {
        return new AttributeValidatorMetadata(message, validator);
    }

    public static final Validator isBlank() {
        return (context) -> {
            Map.Entry<String, List<String>> attribute = context.getAttribute();
            List<String> values = attribute.getValue();

            if (values.isEmpty()) {
                return true;
            }

            String value = values.get(0);

            return value == null || !Validation.isBlank(value);
        };
    }

    public static final Validator isEmailValid() {
        return (context) -> {
            Map.Entry<String, List<String>> attribute = context.getAttribute();
            List<String> values = attribute.getValue();

            if (values.isEmpty()) {
                return true;
            }

            String value = values.get(0);

            return Validation.isBlank(value) || Validation.isEmailValid(value);
        };
    }

    public static final Validator userNameExists() {
        return (context) -> {
            Map.Entry<String, List<String>> attribute = context.getAttribute();
            List<String> values = attribute.getValue();

            if (values.isEmpty()) {
                return true;
            }

            String value = values.get(0);

            if (Validation.isBlank(value)) return true;

            KeycloakSession session = context.getSession();
            UserModel existing = session.users().getUserByUsername(session.getContext().getRealm(), value);
            UserModel user = context.getUser();

            return !(user != null
                    && !value.equals(user.getFirstAttribute(UserModel.USERNAME))
                    && (existing != null && !existing.getId().equals(user.getId())));
        };
    }

    public static final Validator isUserMutable() {
        return (context) -> {
            Map.Entry<String, List<String>> attribute = context.getAttribute();
            List<String> values = attribute.getValue();

            if (values.isEmpty()) {
                return true;
            }

            String value = values.get(0);

            if (Validation.isBlank(value)) return true;

            UserModel user = context.getUser();
            RealmModel realm = context.getSession().getContext().getRealm();

            return !(!realm.isEditUsernameAllowed()
                        && user != null
                        && !value.equals(user.getFirstAttribute(UserModel.USERNAME))
                );
        };
    }

    public static final Validator checkFederatedUsernameExists() {
        return (context) -> {
            Map.Entry<String, List<String>> attribute = context.getAttribute();
            List<String> values = attribute.getValue();
            String value = null;

            if (!values.isEmpty()) {
                value = values.get(0);
            }

            RealmModel realm = context.getSession().getContext().getRealm();

            return !(!realm.isRegistrationEmailAsUsername() && Validation.isBlank(value));
        };
    }

    public static final Validator checkUsernameExists() {
        return (context) -> {
            Map.Entry<String, List<String>> attribute = context.getAttribute();
            List<String> values = attribute.getValue();
            String value = null;

            if (!values.isEmpty()) {
                value = values.get(0);
            }

            return !Validation.isBlank(value);
        };
    }

    public static final Validator doesEmailExistAsUsername() {
        return (context) -> {
            Map.Entry<String, List<String>> attribute = context.getAttribute();
            List<String> values = attribute.getValue();

            if (values.isEmpty()) {
                return true;
            }

            String value = values.get(0);

            if (Validation.isBlank(value)) return true;

            KeycloakSession session = context.getSession();
            RealmModel realm = session.getContext().getRealm();
            UserModel user = context.getUser();

            if (!realm.isDuplicateEmailsAllowed()) {
                UserModel userByEmail = session.users().getUserByEmail(realm, value);
                return !(realm.isRegistrationEmailAsUsername() && userByEmail != null && user != null && !userByEmail.getId().equals(user.getId()));
            }
            return true;
        };
    }

    public static final Validator isEmailDuplicated() {
        return (context) -> {
            Map.Entry<String, List<String>> attribute = context.getAttribute();
            List<String> values = attribute.getValue();

            if (values.isEmpty()) {
                return true;
            }

            String value = values.get(0);

            if (Validation.isBlank(value)) return true;

            KeycloakSession session = context.getSession();
            RealmModel realm = session.getContext().getRealm();

            if (!realm.isDuplicateEmailsAllowed()) {
                UserModel userByEmail = session.users().getUserByEmail(realm, value);
                UserModel user = context.getUser();
                // check for duplicated email
                return !(userByEmail != null && (user == null || !userByEmail.getId().equals(user.getId())));
            }
            return true;
        };
    }

    public static final Validator doesEmailExist(KeycloakSession session) {
        return (context) -> {
            if (UserProfileContext.REGISTRATION_USER_CREATION.equals(context.getContext())) {
                RealmModel realm = context.getSession().getContext().getRealm();

                if (!realm.isRegistrationEmailAsUsername()) {
                    return true;
                }
            }

            Map.Entry<String, List<String>> attribute = context.getAttribute();
            List<String> values = attribute.getValue();
            String value = values.get(0);

            return !(value != null
                    && !session.getContext().getRealm().isDuplicateEmailsAllowed()
                    && session.users().getUserByEmail(session.getContext().getRealm(), value) != null);
        };
    }
}
