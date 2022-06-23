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

package org.keycloak.quarkus.runtime.configuration.mappers;

import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;

import org.keycloak.config.StorageOptions;
import org.keycloak.quarkus.runtime.configuration.Configuration;

import io.smallrye.config.ConfigSourceInterceptorContext;

final class StoragePropertyMappers {

    private StoragePropertyMappers(){}

    public static PropertyMapper[] getMappers() {
        return new PropertyMapper[] {
                fromOption(StorageOptions.DEFAULT_PERSISTENCE_UNIT_ENABLED)
                        .to("kc.spi-connections-jpa-quarkus-enabled")
                        .paramLabel(Boolean.TRUE + "|" + Boolean.FALSE)
                        .transformer(StoragePropertyMappers::isLegacyStorageEnabled)
                        .build(),
                fromOption(StorageOptions.STORAGE)
                        .to("kc.spi-map-storage-provider")
                        .transformer(StoragePropertyMappers::resolveStorageProvider)
                        .paramLabel("type")
                        .build(),
                fromOption(StorageOptions.STORAGE_REALM)
                        .to("kc.spi-realm-provider")
                        .mapFrom("storage")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .paramLabel("type")
                        .build(),
                fromOption(StorageOptions.STORAGE_CLIENT)
                        .to("kc.spi-client-provider")
                        .mapFrom("storage")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .paramLabel("type")
                        .build(),
                fromOption(StorageOptions.STORAGE_CLIENT_SCOPE)
                        .to("kc.spi-client-scope-provider")
                        .mapFrom("storage")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .paramLabel("type")
                        .build(),
                fromOption(StorageOptions.STORAGE_GROUP)
                        .to("kc.spi-group-provider")
                        .mapFrom("storage")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .paramLabel("type")
                        .build(),
                fromOption(StorageOptions.STORAGE_ROLE)
                        .to("kc.spi-role-provider")
                        .mapFrom("storage")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .paramLabel("type")
                        .build(),
                fromOption(StorageOptions.STORAGE_USER)
                        .to("kc.spi-user-provider")
                        .mapFrom("storage")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .paramLabel("type")
                        .build(),
                fromOption(StorageOptions.STORAGE_DEPLOYMENT_STATE)
                        .to("kc.spi-deployment-state-provider")
                        .mapFrom("storage")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .paramLabel("type")
                        .build(),
                fromOption(StorageOptions.STORAGE_AUTH_SESSION)
                        .to("kc.spi-auth-session-provider")
                        .mapFrom("storage")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .paramLabel("type")
                        .build(),
                fromOption(StorageOptions.STORAGE_USER_SESSION)
                        .to("kc.spi-user-session-provider")
                        .mapFrom("storage")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .paramLabel("type")
                        .build(),
                fromOption(StorageOptions.STORAGE_LOGIN_FAILURE)
                        .to("kc.spi-login-failure-provider")
                        .mapFrom("storage")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .paramLabel("type")
                        .build(),
                fromOption(StorageOptions.STORAGE_AUTHORIZATION_PERSISTER)
                        .to("kc.spi-authorization-persister-provider")
                        .mapFrom("storage")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .paramLabel("type")
                        .build(),
                fromOption(StorageOptions.STORAGE_DBLOCK)
                        .to("kc.spi-dblock-provider")
                        .mapFrom("storage")
                        .transformer(StoragePropertyMappers::getDbLockProvider)
                        .paramLabel("type")
                        .build()
        };
    }

    private static String getAreaStorage(String storage, ConfigSourceInterceptorContext context) {
        return "chms".equals(storage) ? "map" : "jpa";
    }

    private static String getDbLockProvider(String storage, ConfigSourceInterceptorContext context) {
        return "chms".equals(storage) ? "none" : "jpa";
    }

    private static String isLegacyStorageEnabled(String s, ConfigSourceInterceptorContext context) {
        String storage = Configuration.getRawValue("kc.storage");

        if (storage == null) {
            return Boolean.TRUE.toString();
        }

        // disables legacy store if a storage mechanism is set
        return Boolean.FALSE.toString();
    }

    private static String resolveStorageProvider(String value, ConfigSourceInterceptorContext context) {
        return "chms".equals(value) ? "concurrenthashmap" : null;
    }
}
