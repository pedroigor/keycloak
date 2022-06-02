package org.keycloak.config;

import java.util.ArrayList;
import java.util.List;

public class VaultOptions {

    public enum Provider {
        file,
        hashicorp;
    }

    public final static Option vault = new OptionBuilder<>("vault", Provider.class)
            .category(OptionCategory.VAULT)
            .description("Enables a vault provider.")
            .buildTime(true)
            .expectedValues(Provider.values())
            .build();

    public final static Option vaultDir = new OptionBuilder<>("vault-dir", String.class)
            .category(OptionCategory.VAULT)
            .description("If set, secrets can be obtained by reading the content of files within the given directory.")
            .build();

    // TODO: verify this option
    public final static Option vaultGeneric = new OptionBuilder<>("vault-", String.class)
            .category(OptionCategory.VAULT)
            .description("Maps any vault option to their corresponding properties in quarkus-vault extension.")
            .runtimes() // TODO: verify me
            .buildTime(true)
            .build();

    public final static Option vaultUrl = new OptionBuilder<>("vault-url", String.class)
            .category(OptionCategory.VAULT)
            .description("The vault server url.")
            .runtimes() // TODO: verify me
            .buildTime(true)
            .build();

    // TODO: should this be a Map? -> it will change the current encoding probably
    public final static Option vaultKvPaths = new OptionBuilder<>("vault-kv-paths", String.class)
            .category(OptionCategory.VAULT)
            .description("A set of one or more key/value paths that should be used when looking up secrets.")
            .runtimes() // TODO: verify me
            .build();

    public final static List<Option<?>> ALL_OPTIONS = new ArrayList<>();

    static {
        ALL_OPTIONS.add(vault);
        ALL_OPTIONS.add(vaultDir);
        ALL_OPTIONS.add(vaultGeneric);
        ALL_OPTIONS.add(vaultUrl);
        ALL_OPTIONS.add(vaultKvPaths);
    }
}
