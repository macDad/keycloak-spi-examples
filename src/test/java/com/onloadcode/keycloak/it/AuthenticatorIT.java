package com.onloadcode.keycloak.it;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class AuthenticatorIT {

    @Container
    static KeycloakContainer keycloak =
            new KeycloakContainer("quay.io/keycloak/keycloak:26.0.7")
                .withProviderClassesFrom("target/classes")
                .withRealmImportFile("test-realm.json");

    @Test
    void customAuthenticatorIsRegistered() {
        var info = keycloak.getKeycloakAdminClient()
                           .serverInfo().getInfo();

        assertThat(info.getProviders())
            .containsKey("authenticator");
    }

    // Not shown in the article: the same serverinfo response also confirms
    // the password policy provider and the identity provider mapper.
    @Test
    void customPasswordPolicyProviderIsRegistered() {
        var info = keycloak.getKeycloakAdminClient()
                           .serverInfo().getInfo();

        assertThat(info.getProviders())
            .containsKey("password-policy");
        assertThat(info.getProviders().get("password-policy").getProviders())
            .containsKey("noIdentityInPassword");
    }

    @Test
    void customIdentityProviderMapperIsRegistered() {
        var info = keycloak.getKeycloakAdminClient()
                           .serverInfo().getInfo();

        assertThat(info.getProviders())
            .containsKey("identity-provider-mapper");
        assertThat(info.getProviders().get("identity-provider-mapper").getProviders())
            .containsKey("nested-claim-to-role-mapper");
    }

    @Test
    void customAuthenticatorHasExpectedId() {
        var info = keycloak.getKeycloakAdminClient()
                           .serverInfo().getInfo();

        assertThat(info.getProviders().get("authenticator").getProviders())
            .containsKey("review-confirm-authenticator");
    }
}
