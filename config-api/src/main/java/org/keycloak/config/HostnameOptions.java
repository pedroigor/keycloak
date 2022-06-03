package org.keycloak.config;

import java.util.ArrayList;
import java.util.List;

public class HostnameOptions {

    public final static Option hostname = new OptionBuilder<>("hostname", String.class)
            .category(OptionCategory.HOSTNAME)
            .description("Hostname for the Keycloak server.")
            .build();

    public final static Option hostnameAdmin = new OptionBuilder<>("hostname-admin", String.class)
            .category(OptionCategory.HOSTNAME)
            .description("The hostname for accessing the administration console. Use this option if you are exposing the administration console using a hostname other than the value set to the 'hostname' option.")
            .build();

    public final static Option hostnameStrict = new OptionBuilder<>("hostname-strict", Boolean.class)
            .category(OptionCategory.HOSTNAME)
            .description("Disables dynamically resolving the hostname from request headers. Should always be set to true in production, unless proxy verifies the Host header.")
            .defaultValue(Boolean.TRUE)
            .build();

    public final static Option hostnameStrictHttps = new OptionBuilder<>("hostname-strict-https", Boolean.class)
            .category(OptionCategory.HOSTNAME)
            .description("Forces URLs to use HTTPS. Only needed if proxy does not properly set the X-Forwarded-Proto header.")
            .runtimes(Option.Runtime.OPERATOR)
            .defaultValue(Boolean.TRUE)
            .build();

    public final static Option hostnameStrictBackchannel = new OptionBuilder<>("hostname-strict-backchannel", Boolean.class)
            .category(OptionCategory.HOSTNAME)
            .description("By default backchannel URLs are dynamically resolved from request headers to allow internal and external applications. If all applications use the public URL this option should be enabled.")
            .build();

    public final static Option hostnamePath = new OptionBuilder<>("hostname-path", String.class)
            .category(OptionCategory.HOSTNAME)
            .description("This should be set if proxy uses a different context-path for Keycloak.")
            .build();

    public final static Option hostnamePort = new OptionBuilder<>("hostname-port", Integer.class)
            .category(OptionCategory.HOSTNAME)
            .description("The port used by the proxy when exposing the hostname. Set this option if the proxy uses a port other than the default HTTP and HTTPS ports.")
            .defaultValue(-1)
            .build();

    public final static List<Option<?>> ALL_OPTIONS = new ArrayList<>();

    static {
        ALL_OPTIONS.add(hostname);
        ALL_OPTIONS.add(hostnameStrict);
        ALL_OPTIONS.add(hostnameStrictHttps);
        ALL_OPTIONS.add(hostnameStrictBackchannel);
        ALL_OPTIONS.add(hostnamePath);
        ALL_OPTIONS.add(hostnamePort);
    }
}
