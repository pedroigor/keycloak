package org.keycloak.config;

public class FipsOptions {

    public static final Option FIPS_ENABLED = new OptionBuilder<>("fips-enabled", Boolean.class)
            .category(OptionCategory.FIPS)
            .buildTime(true)
            .hidden()
            .description("Enable FIPS support.")
            .build();
}
