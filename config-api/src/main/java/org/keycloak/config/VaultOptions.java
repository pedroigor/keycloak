package org.keycloak.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public final static Option vaultDir = new OptionBuilder<>("vault-dir", File.class)
            .category(OptionCategory.VAULT)
            .description("If set, secrets can be obtained by reading the content of files within the given directory.")
            .build();

    public final static Option vaultGeneric = new OptionBuilder<>("vault-", String.class)
            .category(OptionCategory.VAULT)
            .description("Maps any vault option to their corresponding properties in quarkus-vault extension.")
            .runtimes() // TODO: verify this is desired
            .buildTime(true)
            .build();

    public final static Option vaultUrl = new OptionBuilder<>("vault-url", String.class)
            .category(OptionCategory.VAULT)
            .description("The vault server url.")
            .runtimes() // TODO: verify this is desired
            .buildTime(true)
            .build();

    public final static Option vaultKvPaths = new OptionBuilder("vault-kv-paths", Map.class, String.class)
            .category(OptionCategory.VAULT)
            .description("A set of one or more key/value paths that should be used when looking up secrets.")
            .runtimes() // TODO: verify this is desired
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
