package org.keycloak.quarkus.runtime.configuration.mappers;

import org.keycloak.config.HostnameOptions;

import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;

final class HostnamePropertyMappers {

    private HostnamePropertyMappers(){}

    public static PropertyMapper[] getHostnamePropertyMappers() {
        return new PropertyMapper[] {
                fromOption(HostnameOptions.hostname)
                        .to("kc.spi-hostname-default-hostname")
                        .paramLabel("hostname")
                        .build(),
                fromOption(HostnameOptions.hostnameAdmin)
                        .to("kc.spi-hostname-default-admin")
                        .paramLabel("hostname")
                        .build(),
                fromOption(HostnameOptions.hostnameStrict)
                        .to("kc.spi-hostname-default-strict")
                        .build(),
                fromOption(HostnameOptions.hostnameStrictHttps)
                        .to("kc.spi-hostname-default-strict-https")
                        .build(),
                fromOption(HostnameOptions.hostnameStrictBackchannel)
                        .to("kc.spi-hostname-default-strict-backchannel")
                        .build(),
                fromOption(HostnameOptions.hostnamePath)
                        .to("kc.spi-hostname-default-path")
                        .paramLabel("path")
                        .build(),
                fromOption(HostnameOptions.hostnamePort)
                        .to("kc.spi-hostname-default-hostname-port")
                        .paramLabel("port")
                        .build()
        };
    }

}
