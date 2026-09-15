package com.onloadcode.keycloak.policy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.policy.PolicyError;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NoIdentityInPasswordProviderTest {

    @Mock
    private RealmModel realm;

    private NoIdentityInPasswordProvider provider;

    @BeforeEach
    void setUp() {
        provider = new NoIdentityInPasswordProvider(null);
    }

    @Test
    void rejectsPasswordContainingUsername() {
        UserModel user = mock(UserModel.class);
        when(user.getUsername()).thenReturn("maduka");

        PolicyError error = provider.validate(realm, user, "Maduka2026!");

        assertThat(error).isNotNull();
    }

    @Test
    void rejectsPasswordContainingEmailLocalPart() {
        UserModel user = mock(UserModel.class);
        when(user.getEmail()).thenReturn("jsmith@example.com");

        PolicyError error = provider.validate(realm, user, "JSmith2026!");

        assertThat(error).isNotNull();
    }

    @Test
    void rejectsPasswordContainingFirstName() {
        UserModel user = mock(UserModel.class);
        when(user.getFirstName()).thenReturn("Nadun");

        PolicyError error = provider.validate(realm, user, "Nadun2026!");

        assertThat(error).isNotNull();
    }

    @Test
    void rejectsPasswordContainingLastName() {
        UserModel user = mock(UserModel.class);
        when(user.getLastName()).thenReturn("Jayawardana");

        PolicyError error = provider.validate(realm, user, "Jayawardana2026!");

        assertThat(error).isNotNull();
    }

    @Test
    void acceptsPasswordNotContainingIdentity() {
        UserModel user = mock(UserModel.class);
        lenient().when(user.getUsername()).thenReturn("maduka");
        lenient().when(user.getEmail()).thenReturn("maduka@example.com");
        lenient().when(user.getFirstName()).thenReturn("Maduka");
        lenient().when(user.getLastName()).thenReturn("Jayawardana");

        PolicyError error = provider.validate(realm, user, "Tr0ub4dor&3");

        assertThat(error).isNull();
    }

    @Test
    void skipsIdentityPartsShorterThanThreeChars() {
        // A two-character username is deliberately not enforced, since
        // that would reject a huge fraction of otherwise-unrelated passwords.
        UserModel user = mock(UserModel.class);
        when(user.getUsername()).thenReturn("ab");

        PolicyError error = provider.validate(realm, user, "abSecurePassword2026!");

        assertThat(error).isNull();
    }

    @Test
    void registrationOverloadRejectsPasswordContainingUsername() {
        PolicyError error = provider.validate("maduka", "Maduka2026!");

        assertThat(error).isNotNull();
    }

    @Test
    void registrationOverloadAcceptsUnrelatedPassword() {
        PolicyError error = provider.validate("maduka", "Tr0ub4dor&3");

        assertThat(error).isNull();
    }

    @Test
    void registrationOverloadSkipsShortUsername() {
        PolicyError error = provider.validate("ab", "abSecurePassword2026!");

        assertThat(error).isNull();
    }
}
