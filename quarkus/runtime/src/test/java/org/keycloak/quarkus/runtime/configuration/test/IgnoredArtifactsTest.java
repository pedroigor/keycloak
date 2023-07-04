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

package org.keycloak.quarkus.runtime.configuration.test;

import org.junit.Test;
import org.keycloak.common.Profile;
import org.keycloak.common.profile.PropertiesProfileConfigResolver;
import org.keycloak.config.StorageOptions;
import org.keycloak.quarkus.runtime.cli.IgnoredArtifacts;
import org.keycloak.quarkus.runtime.configuration.MicroProfileConfigProvider;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class IgnoredArtifactsTest {

    private static final Set<String> EXCLUDED_ARTIFACTS_NO_FIPS = Set.of(
            "org.keycloak:keycloak-crypto-fips1402",
            "org.bouncycastle:bc-fips",
            "org.bouncycastle:bctls-fips",
            "org.bouncycastle:bcpkix-fips"
    );

    private static final Set<String> EXCLUDED_ARTIFACTS_FIPS = Set.of(
            "org.bouncycastle:bcprov-jdk15on",
            "org.bouncycastle:bcpkix-jdk15on",
            "org.bouncycastle:bcutil-jdk15on",
            "org.keycloak:keycloak-crypto-default"
    );

    private static final Set<String> EXCLUDED_ARTIFACTS_MAP_STORAGE = Set.of(
            "org.keycloak:keycloak-model-map-jpa",
            "org.keycloak:keycloak-model-map-hot-rod",
            "org.keycloak:keycloak-model-map",
            "org.keycloak:keycloak-model-map-file"
    );

    @Test
    public void fipsDisabled() {
        var profile = Profile.defaults();
        assertThat(profile.isFeatureEnabled(Profile.Feature.FIPS), is(false));

        var ignoredArtifacts = IgnoredArtifacts.getDefaultIgnoredArtifacts();
        assertThat(ignoredArtifacts.containsAll(EXCLUDED_ARTIFACTS_NO_FIPS), is(true));
    }

    @Test
    public void fipsEnabled() {
        Properties properties = new Properties();
        properties.setProperty("keycloak.profile.feature.fips", "enabled");
        var profile = Profile.configure(new PropertiesProfileConfigResolver(properties));

        assertThat(profile.isFeatureEnabled(Profile.Feature.FIPS), is(true));

        var ignoredArtifacts = IgnoredArtifacts.getDefaultIgnoredArtifacts();
        assertThat(ignoredArtifacts.containsAll(EXCLUDED_ARTIFACTS_FIPS), is(true));
    }

    @Test
    public void ignoredMapStorage() {
        var ignoredArtifacts = IgnoredArtifacts.getDefaultIgnoredArtifacts();
        assertThat(ignoredArtifacts.containsAll(EXCLUDED_ARTIFACTS_MAP_STORAGE), is(true));

        BiConsumer<String, String> assertStorage = (storage, artifact) -> {
            System.setProperty(MicroProfileConfigProvider.NS_KEYCLOAK_PREFIX + StorageOptions.STORAGE.getKey(), storage);

            try {
                final var artifacts = IgnoredArtifacts.getDefaultIgnoredArtifacts();
                final var without = new HashSet<>(EXCLUDED_ARTIFACTS_MAP_STORAGE);

                without.remove("org.keycloak:keycloak-model-map");
                without.remove(artifact);

                assertThat(artifacts, hasItems(without.toArray(new String[0])));
                assertThat(artifacts, not(hasItems(artifact)));
            } finally {
                System.setProperty(MicroProfileConfigProvider.NS_KEYCLOAK_PREFIX + StorageOptions.STORAGE.getKey(), "");
            }
        };

        assertStorage.accept("jpa", "org.keycloak:keycloak-model-map-jpa");
        assertStorage.accept("hotrod", "org.keycloak:keycloak-model-map-hot-rod");
        assertStorage.accept("file", "org.keycloak:keycloak-model-map-file");
        assertStorage.accept("chm", "");
    }
}
