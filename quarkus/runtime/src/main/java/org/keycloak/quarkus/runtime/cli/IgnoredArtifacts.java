/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.quarkus.runtime.cli;

import org.keycloak.common.Profile;
import org.keycloak.config.StorageOptions;
import org.keycloak.quarkus.runtime.configuration.Configuration;
import org.keycloak.quarkus.runtime.configuration.MicroProfileConfigProvider;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.keycloak.quarkus.runtime.Environment.getCurrentOrCreateFeatureProfile;

/**
 * Ignore particular artifacts based on build configuration
 */
public class IgnoredArtifacts {

    public static Set<String> getDefaultIgnoredArtifacts() {
        return new Builder()
                .append(fips())
                .append(storage())
                .build();
    }

    private static Set<String> fips() {
        final Profile profile = getCurrentOrCreateFeatureProfile();
        if (profile.getFeatures().get(Profile.Feature.FIPS)) {
            return Set.of(
                    "org.bouncycastle:bcprov-jdk15on",
                    "org.bouncycastle:bcpkix-jdk15on",
                    "org.bouncycastle:bcutil-jdk15on",
                    "org.keycloak:keycloak-crypto-default");
        } else {
            return Set.of(
                    "org.keycloak:keycloak-crypto-fips1402",
                    "org.bouncycastle:bc-fips",
                    "org.bouncycastle:bctls-fips",
                    "org.bouncycastle:bcpkix-fips");
        }
    }

    private static Set<String> storage() {
        Optional<String> storage = Configuration.getOptionalValue(
                MicroProfileConfigProvider.NS_KEYCLOAK_PREFIX + StorageOptions.STORAGE.getKey());

        Set<String> excludedArtifacts = new HashSet<>(Set.of(
                "org.keycloak:keycloak-model-map-jpa",
                "org.keycloak:keycloak-model-map-hot-rod",
                "org.keycloak:keycloak-model-map",
                "org.keycloak:keycloak-model-map-file"
        ));

        if (storage.isEmpty()) return excludedArtifacts;

        Optional<StorageOptions.StorageType> storageType = StorageOptions.StorageType.getStorageType(storage.get());
        if (storageType.isEmpty()) return excludedArtifacts;

        excludedArtifacts.remove("org.keycloak:keycloak-model-map");

        switch (storageType.get()) {
            case jpa -> excludedArtifacts.remove("org.keycloak:keycloak-model-map-jpa");
            case hotrod -> excludedArtifacts.remove("org.keycloak:keycloak-model-map-hot-rod");
            case file -> excludedArtifacts.remove("org.keycloak:keycloak-model-map-file");
        }

        return excludedArtifacts;
    }

    private static final class Builder {
        Set<String> finalIgnoredArtifacts;

        public Builder() {
            this.finalIgnoredArtifacts = new HashSet<>();
        }

        public Builder append(Set<String> ignoredArtifacts) {
            finalIgnoredArtifacts.addAll(ignoredArtifacts);
            return this;
        }

        public Set<String> build() {
            return finalIgnoredArtifacts;
        }
    }
}
