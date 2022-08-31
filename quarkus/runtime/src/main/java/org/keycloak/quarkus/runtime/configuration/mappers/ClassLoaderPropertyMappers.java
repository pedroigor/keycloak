package org.keycloak.quarkus.runtime.configuration.mappers;

import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;

import java.util.Optional;
import org.keycloak.config.ClassLoaderOptions;
import org.keycloak.config.FipsOptions;
import org.keycloak.quarkus.runtime.Environment;
import org.keycloak.quarkus.runtime.configuration.Configuration;
import org.keycloak.quarkus.runtime.configuration.MicroProfileConfigProvider;

import io.smallrye.config.ConfigSourceInterceptorContext;
import io.smallrye.config.ConfigValue;

final class ClassLoaderPropertyMappers {

    private ClassLoaderPropertyMappers(){}

    public static PropertyMapper[] getMappers() {
        return new PropertyMapper[] {
                fromOption(ClassLoaderOptions.IGNORE_ARTIFACTS)
                        .to("quarkus.class-loading.removed-artifacts")
                        .transformer(ClassLoaderPropertyMappers::resolveIgnoredArtifacts)
                        .paramLabel("mode")
                        .build()
        };
    }

    private static Optional<String> resolveIgnoredArtifacts(Optional<String> value, ConfigSourceInterceptorContext context) {
        if (Environment.isRebuildCheck()) {
            ConfigValue fipsEnabled = Configuration.getConfigValue(
                    MicroProfileConfigProvider.NS_KEYCLOAK_PREFIX + FipsOptions.FIPS_ENABLED.getKey());

            if (fipsEnabled != null && Boolean.parseBoolean(fipsEnabled.getValue())) {
                return Optional.of(
                        "org.bouncycastle:bcprov-jdk15on,org.bouncycastle:bcpkix-jdk15on,org.keycloak:keycloak-crypto-default");
            }

            return Optional.of(
                    "org.keycloak:keycloak-crypto-fips1402,org.bouncycastle:bc-fips,org.bouncycastle:bctls-fips,org.bouncycastle:bcpkix-fips");
        }

        return value;
    }
}
