package com.onloadcode.keycloak.policy;

import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.policy.PasswordPolicyProvider;
import org.keycloak.policy.PolicyError;

public class NoIdentityInPasswordProvider implements PasswordPolicyProvider {

    private final KeycloakSession session;

    public NoIdentityInPasswordProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public PolicyError validate(RealmModel realm, UserModel user, String password) {
        Stream<String> identityParts = Stream.of(
                user.getUsername(),
                localPart(user.getEmail()),
                user.getFirstName(),
                user.getLastName());

        String lower = password.toLowerCase(Locale.ROOT);

        boolean hit = identityParts
                .filter(Objects::nonNull)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .filter(s -> s.length() >= 3)
                .anyMatch(lower::contains);

        return hit ? new PolicyError("invalidPasswordIdentityMessage") : null;
    }

    @Override
    public PolicyError validate(String username, String password) {
        if (username != null && username.length() >= 3
                && password.toLowerCase(Locale.ROOT)
                           .contains(username.toLowerCase(Locale.ROOT))) {
            return new PolicyError("invalidPasswordIdentityMessage");
        }
        return null;
    }

    @Override
    public Object parseConfig(String value) {
        return null;   // this policy takes no configuration
    }

    @Override
    public void close() { }

    private static String localPart(String email) {
        return email == null ? null : email.split("@")[0];
    }
}
