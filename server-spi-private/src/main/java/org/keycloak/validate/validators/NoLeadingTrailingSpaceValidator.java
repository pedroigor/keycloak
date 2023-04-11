/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.validate.validators;

import java.util.Collections;
import java.util.List;

import org.keycloak.provider.ConfiguredProvider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.validate.AbstractStringValidator;
import org.keycloak.validate.ValidationContext;
import org.keycloak.validate.ValidationError;
import org.keycloak.validate.ValidatorConfig;

public class NoLeadingTrailingSpaceValidator extends AbstractStringValidator implements ConfiguredProvider {

    public static final String ID = "no-leading-trailing-space";

    private static final String ERROR_MSG = "error-invalid-space";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    protected void doValidate(String value, String inputHint, ValidationContext context, ValidatorConfig config) {
        if (hasSpaces(value)) {
            context.addError(new ValidationError(ID, inputHint, ERROR_MSG, value));
        }
    }

    private static boolean hasSpaces(String value) {
        return Character.isSpaceChar(value.charAt(0)) || Character.isSpaceChar(value.charAt(value.length() - 1));
    }

    @Override
    public String getHelpText() {
        return "Do not allow leading or trailing spaces in strings";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }
}
