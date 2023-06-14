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

package org.keycloak.quarkus.runtime;

import static org.keycloak.config.StorageOptions.STORAGE;
import static org.keycloak.quarkus.runtime.configuration.Configuration.getOptionalValue;
import static org.keycloak.quarkus.runtime.configuration.MicroProfileConfigProvider.NS_KEYCLOAK_PREFIX;

import java.util.List;
import java.util.stream.Collectors;
import org.keycloak.models.map.storage.MapStorageSpi;
import org.keycloak.models.map.storage.jpa.liquibase.connection.MapLiquibaseConnectionSpi;
import org.keycloak.models.map.storage.jpa.updater.MapJpaUpdaterSpi;
import org.keycloak.provider.KeycloakDeploymentInfo;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.ProviderManager;
import org.keycloak.provider.Spi;

public final class Providers {

    private static final List<Class<? extends Spi>> JPA_STORAGE_SPI = List.of(
            MapLiquibaseConnectionSpi.class,
            MapJpaUpdaterSpi.class
    );

    private static final List<Class<? extends Spi>> STORAGE_SPI = List.of(
            MapStorageSpi.class
    );

    public static ProviderManager getProviderManager(ClassLoader classLoader) {
        KeycloakDeploymentInfo keycloakDeploymentInfo = KeycloakDeploymentInfo.create()
                .name("classpath")
                .services()
                .themeResources();

        return new ProviderManager(keycloakDeploymentInfo, classLoader) {
            @Override
            public synchronized List<Spi> loadSpis() {
                if (isLegacyStore()) {
                    // do not include SPIs from keycloak-model-map-jpa in the runtime classpath if using legacy store
                    return super.loadSpis().stream()
                            .filter(spi -> !STORAGE_SPI.contains(spi.getClass()))
                            .filter(spi -> !JPA_STORAGE_SPI.contains(spi.getClass()))
                            .collect(Collectors.toList());
                }

                return super.loadSpis();
            }

            @Override
            public synchronized List<ProviderFactory> load(Spi spi) {
                if (isLegacyStore()) {
                    return super.load(spi)
                            .stream()
                            .filter(f -> !f.getClass().getName().startsWith("org.keycloak.models.map.storage.jpa"))
                            .collect(Collectors.toList());
                }

                return super.load(spi);
            }

            private boolean isLegacyStore() {
                return getOptionalValue(NS_KEYCLOAK_PREFIX.concat(STORAGE.getKey())).isEmpty();
            }
        };
    }
}
