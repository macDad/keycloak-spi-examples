package com.onloadcode.keycloak.authenticator;

import java.util.List;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

public class ReviewConfirmAuthenticatorFactory implements AuthenticatorFactory {

    public static final String ID = "review-confirm-authenticator";

    private static final AuthenticationExecutionModel.Requirement[] CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override public String getId() { return ID; }
    @Override public String getDisplayType() { return "Review Confirmation"; }
    @Override public String getReferenceCategory() { return null; }
    @Override public boolean isConfigurable() { return false; }
    @Override public boolean isUserSetupAllowed() { return false; }
    @Override public String getHelpText() {
        return "Requires flagged users to confirm a review screen.";
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return CHOICES;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new ReviewConfirmAuthenticator();
    }

    @Override public List<ProviderConfigProperty> getConfigProperties() {
        return List.of();
    }
    @Override public void init(Config.Scope config) { }
    @Override public void postInit(KeycloakSessionFactory factory) { }
    @Override public void close() { }
}
