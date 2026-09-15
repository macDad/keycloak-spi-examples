package com.onloadcode.keycloak.authenticator;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class ReviewConfirmAuthenticator implements Authenticator {

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        UserModel user = context.getUser();

        if (!Boolean.parseBoolean(user.getFirstAttribute("requiresReview"))) {
            context.success();   // not applicable — move on
            return;
        }

        Response challenge = context.form()
                .createForm("review-confirm.ftl");
        context.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> form =
                context.getHttpRequest().getDecodedFormParameters();

        if (!"true".equals(form.getFirst("accepted"))) {
            context.failureChallenge(
                    AuthenticationFlowError.INVALID_CREDENTIALS,
                    context.form()
                           .setError("reviewNotAccepted")
                           .createForm("review-confirm.ftl"));
            return;
        }

        context.getUser().removeAttribute("requiresReview");
        context.success();
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession s, RealmModel r, UserModel u) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession s, RealmModel r, UserModel u) { }

    @Override
    public void close() { }
}
