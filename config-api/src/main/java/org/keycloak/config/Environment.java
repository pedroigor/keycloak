package org.keycloak.config;

public class Environment {

    public static String getHomeDir() {
        return System.getProperty("kc.home.dir");
    }

}
