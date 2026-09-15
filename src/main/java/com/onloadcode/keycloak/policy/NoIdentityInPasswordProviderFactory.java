package com.onloadcode.keycloak.policy;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.policy.PasswordPolicyProvider;
import org.keycloak.policy.PasswordPolicyProviderFactory;

public class NoIdentityInPasswordProviderFactory
        implements PasswordPolicyProviderFactory {

    public static final String ID = "noIdentityInPassword";

    @Override public String getId() { return ID; }
    @Override public String getDisplayName() { return "Not Containing Identity"; }
    @Override public String getConfigType() { return null; }
    @Override public String getDefaultConfigValue() { return null; }
    @Override public boolean isMultiplSupported() { return false; }

    @Override
    public PasswordPolicyProvider create(KeycloakSession session) {
        return new NoIdentityInPasswordProvider(session);
    }

    @Override public void init(Config.Scope config) { }
    @Override public void postInit(KeycloakSessionFactory factory) { }
    @Override public void close() { }
}
