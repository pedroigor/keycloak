package org.keycloak.quarkus.runtime.configuration.mappers;

import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;

import io.smallrye.config.ConfigSourceInterceptorContext;
import io.smallrye.config.ConfigValue;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.keycloak.config.ClassLoaderOptions;
import org.keycloak.config.SecurityOptions;
import org.keycloak.config.StorageOptions;
import org.keycloak.quarkus.runtime.Environment;
import org.keycloak.quarkus.runtime.configuration.Configuration;
import org.keycloak.quarkus.runtime.configuration.MicroProfileConfigProvider;

final class ClassLoaderPropertyMappers {

    private static final List<String> CRYPTO_FIPS_ARTIFACTS = List.of(
            "org.keycloak:keycloak-crypto-fips1402", "org.bouncycastle:bc-fips", "org.bouncycastle:bctls-fips",
            "org.bouncycastle:bcpkix-fips");
    private static final List<String> CRYPTO_DEFAULT_ARTIFACTS = List.of(
            "org.bouncycastle:bcprov-jdk15on", "org.bouncycastle:bcpkix-jdk15on", "org.bouncycastle:bcutil-jdk15on",
            "org.keycloak:keycloak-crypto-default");
    private static final List<String> STORAGE_ARTIFACTS = List.of("org.keycloak:keycloak-model-map",
            "org.keycloak:keycloak-model-map-file",
            "org.keycloak:keycloak-model-map-jpa",
            "org.keycloak:keycloak-model-map-hot-rod");

    private ClassLoaderPropertyMappers(){}

    public static PropertyMapper[] getMappers() {
        return new PropertyMapper[] {
                fromOption(ClassLoaderOptions.IGNORE_ARTIFACTS)
                        .to("quarkus.class-loading.removed-artifacts")
                        .transformer(ClassLoaderPropertyMappers::resolveIgnoredArtifacts)
                        .build()
        };
    }

    private static Optional<String> resolveIgnoredArtifacts(Optional<String> value, ConfigSourceInterceptorContext context) {
        if (Environment.isRebuildCheck() || Environment.isRebuild()) {
            Set<String> ignoredArtifacts = new HashSet<>();
            ConfigValue fipsMode = Configuration.getCurrentBuiltTimeProperty(MicroProfileConfigProvider.NS_KEYCLOAK_PREFIX + SecurityOptions.FIPS_MODE.getKey());

            if (fipsMode == null || fipsMode.getValue() == null) {
                ignoredArtifacts.addAll(CRYPTO_FIPS_ARTIFACTS);
            } else {
                ignoredArtifacts.addAll(CRYPTO_DEFAULT_ARTIFACTS);
            }

            ConfigValue storage = Configuration.getCurrentBuiltTimeProperty(
                    MicroProfileConfigProvider.NS_KEYCLOAK_PREFIX + StorageOptions.STORAGE.getKey());

            if (storage == null || storage.getValue() == null) {
                ignoredArtifacts.addAll(STORAGE_ARTIFACTS);
            }

            return Optional.of(String.join(",", ignoredArtifacts));
        }

        return value;
    }
}
