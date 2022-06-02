package org.keycloak.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProxyOptions {

    public enum Mode {
        none,
        edge,
        reencrypt,
        passthrough;
    }

    public final static Option proxy = new OptionBuilder<>("proxy", Mode.class)
            .category(OptionCategory.PROXY)
            .description("The proxy address forwarding mode if the server is behind a reverse proxy. " +
                    "Possible values are: " + String.join(",", Arrays.stream(Mode.values()).skip(1).map(m -> m.name()).collect(Collectors.joining(","))))
            .defaultValue(Mode.none)
            .expectedValues(Mode.values())
            .build();

    // TODO: check originally missing "from"
    public final static Option proxyForwardedHost = new OptionBuilder<>("proxy-forwarded-host", Boolean.class)
            .category(OptionCategory.PROXY)
            .defaultValue(Boolean.FALSE)
            .build();

    public final static List<Option<?>> ALL_OPTIONS = new ArrayList<>();

    static {
        ALL_OPTIONS.add(proxy);
        ALL_OPTIONS.add(proxyForwardedHost);
    }
}
