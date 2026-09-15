package com.onloadcode.keycloak.mapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.keycloak.broker.provider.AbstractIdentityProviderMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

public class NestedClaimToRoleMapper extends AbstractIdentityProviderMapper {

    public static final String ID = "nested-claim-to-role-mapper";
    private static final String CLAIM_PATH = "claim.path";
    private static final String ROLE_PREFIX = "role.prefix";

    // Not shown in the article: getConfigProperties() is abstract in
    // ConfiguredProvider (via IdentityProviderMapper) and has no default in
    // AbstractIdentityProviderMapper, so something has to describe the two
    // config keys the mapper actually reads below.
    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = List.of(
            new ProviderConfigProperty(
                    CLAIM_PATH,
                    "Claim path",
                    "Dot-separated path to the nested claim, e.g. resource.access.fleet.roles",
                    ProviderConfigProperty.STRING_TYPE,
                    null),
            new ProviderConfigProperty(
                    ROLE_PREFIX,
                    "Role prefix",
                    "Prefix prepended to each claim value before looking up the realm role.",
                    ProviderConfigProperty.STRING_TYPE,
                    ""));

    @Override public String getId() { return ID; }
    @Override public String getDisplayType() { return "Nested Claim To Role"; }
    @Override public String getDisplayCategory() { return "Role Importer"; }
    @Override public String getHelpText() {
        return "Maps values from a nested claim path to realm roles.";
    }

    @Override
    public String[] getCompatibleProviders() {
        return new String[]{ "oidc", "keycloak-oidc" };
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public void importNewUser(KeycloakSession session, RealmModel realm,
                              UserModel user, IdentityProviderMapperModel mapper,
                              BrokeredIdentityContext context) {
        applyRoles(realm, user, mapper, context);
    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm,
                                   UserModel user, IdentityProviderMapperModel mapper,
                                   BrokeredIdentityContext context) {
        applyRoles(realm, user, mapper, context);
    }

    private void applyRoles(RealmModel realm, UserModel user,
                            IdentityProviderMapperModel mapper,
                            BrokeredIdentityContext context) {

        String path = mapper.getConfig().get(CLAIM_PATH);
        String prefix = mapper.getConfig().getOrDefault(ROLE_PREFIX, "");

        Object node = context.getContextData().get("UNTRUSTED_ID_TOKEN_CLAIMS");

        for (String segment : path.split("\\.")) {
            if (!(node instanceof Map<?, ?> map)) return;   // path broken — stop
            node = map.get(segment);
        }

        if (node == null) return;

        toStream(node)
            .map(v -> realm.getRole(prefix + v))
            .filter(Objects::nonNull)
            .forEach(user::grantRole);
    }

    // Not shown in the article: called on the value found at the end of the
    // claim path, which may be a single scalar or a JSON array.
    private static Stream<?> toStream(Object node) {
        if (node instanceof List<?> list) {
            return list.stream();
        }
        return Stream.of(node);
    }
}
