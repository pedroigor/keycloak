package org.keycloak.quarkus.runtime.configuration.mappers;

import org.keycloak.config.Option;
import org.keycloak.config.OptionCategory;
import org.keycloak.config.VaultOptions;

import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;

final class VaultPropertyMappers {

    private VaultPropertyMappers() {
    }

    public static PropertyMapper[] getVaultPropertyMappers() {
        return new PropertyMapper[] {
                fromOption(VaultOptions.vault)
                        .paramLabel("provider")
                        .build(),
                fromOption(VaultOptions.vaultDir)
                        .to("kc.spi-vault-file-dir")
                        .paramLabel("dir")
                        .build(),
                fromOption(VaultOptions.vaultGeneric)
                        .to("quarkus.vault.")
                        .build(),
                fromOption(VaultOptions.vaultUrl)
                        .to("quarkus.vault.url")
                        .paramLabel("paths")
                        .build(),
                fromOption(VaultOptions.vaultKvPaths)
                        .to("kc.spi-vault-hashicorp-paths")
                        .paramLabel("paths")
                        .build()
        };
    }

}
