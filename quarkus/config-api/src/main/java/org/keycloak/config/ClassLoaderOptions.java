package org.keycloak.config;

public class ClassLoaderOptions {

    public static final Option<String> IGNORE_ARTIFACTS = new OptionBuilder<>("class-loader-ignore-artifacts", String.class)
            .category(OptionCategory.PROXY)
            .hidden()
            .description("A list of GA referencing the artifacts that should be ignored by the runtime classloader.")
            .build();
}
