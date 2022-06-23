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

package org.keycloak.config;

import java.util.ArrayList;
import java.util.List;

public class StorageOptions {

    public static final Option<Boolean> DEFAULT_PERSISTENCE_UNIT_ENABLED = new OptionBuilder<>("storage-default-persistence-unit-enabled", Boolean.class)
            .category(OptionCategory.STORAGE)
            .defaultValue(true)
            .runtimes(Option.Runtime.OPERATOR)
            .buildTime(true)
            .build();

    public static final Option<String> STORAGE = new OptionBuilder<>("storage", String.class)
            .description("Sets a storage mechanism.")
            .expectedValues("chms")
            .runtimes(Option.Runtime.OPERATOR)
            .buildTime(true)
            .build();

    public static final Option<String> STORAGE_REALM = new OptionBuilder<>("storage-realm", String.class)
            .runtimes(Option.Runtime.OPERATOR)
            .buildTime(true)
            .build();

    public static final Option<String> STORAGE_CLIENT = new OptionBuilder<>("storage-client", String.class)
            .runtimes(Option.Runtime.OPERATOR)
            .buildTime(true)
            .build();

    public static final Option<String> STORAGE_CLIENT_SCOPE = new OptionBuilder<>("storage-client-scope", String.class)
            .runtimes(Option.Runtime.OPERATOR)
            .buildTime(true)
            .build();

    public static final Option<String> STORAGE_GROUP = new OptionBuilder<>("storage-group", String.class)
            .runtimes(Option.Runtime.OPERATOR)
            .buildTime(true)
            .build();

    public static final Option<String> STORAGE_ROLE = new OptionBuilder<>("storage-role", String.class)
            .runtimes(Option.Runtime.OPERATOR)
            .buildTime(true)
            .build();

    public static final Option<String> STORAGE_USER = new OptionBuilder<>("storage-user", String.class)
            .runtimes(Option.Runtime.OPERATOR)
            .buildTime(true)
            .build();

    public static final Option<String> STORAGE_DEPLOYMENT_STATE = new OptionBuilder<>("storage-deployment-state", String.class)
            .runtimes(Option.Runtime.OPERATOR)
            .buildTime(true)
            .build();

    public static final Option<String> STORAGE_AUTH_SESSION = new OptionBuilder<>("storage-auth-session", String.class)
            .runtimes(Option.Runtime.OPERATOR)
            .buildTime(true)
            .build();

    public static final Option<String> STORAGE_USER_SESSION = new OptionBuilder<>("storage-user-session", String.class)
            .runtimes(Option.Runtime.OPERATOR)
            .buildTime(true)
            .build();

    public static final Option<String> STORAGE_LOGIN_FAILURE = new OptionBuilder<>("storage-login-failure", String.class)
            .runtimes(Option.Runtime.OPERATOR)
            .buildTime(true)
            .build();

    public static final Option<String> STORAGE_AUTHORIZATION_PERSISTER = new OptionBuilder<>("storage-authorization-persister", String.class)
            .runtimes(Option.Runtime.OPERATOR)
            .buildTime(true)
            .build();

    public static final Option<String> STORAGE_DBLOCK = new OptionBuilder<>("storage-dblock", String.class)
            .runtimes(Option.Runtime.OPERATOR)
            .buildTime(true)
            .build();

    public static final List<Option<?>> ALL_OPTIONS = new ArrayList<>();

    static {
        ALL_OPTIONS.add(STORAGE);
    }
}
