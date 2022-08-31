package org.keycloak.quarkus.runtime.configuration.mappers;

import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;

import org.keycloak.config.FipsOptions;

final class FipsPropertyMappers {

    private FipsPropertyMappers(){}

    public static PropertyMapper[] getMappers() {
        return new PropertyMapper[] {
                fromOption(FipsOptions.FIPS_ENABLED)
                        .build()
        };
    }
}
