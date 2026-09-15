# keycloak-spi-examples

Working Keycloak 26 custom SPI examples in Java 21: a custom authenticator, a
password policy provider, and a nested-claim identity provider mapper - with
unit tests and a Testcontainers integration test.

Companion code for [Keycloak Custom SPIs: Authenticators, Password Policies and IdP Mappers](https://www.onloadcode.com/post/keycloak-custom-spis-authenticators-password-policies-idp-mappers/)
on OnloadCode.

## What's here

- **`ReviewConfirmAuthenticator`** - adds a confirmation screen for accounts
  flagged with a `requiresReview` attribute (`org.keycloak.authentication.AuthenticatorFactory`)
- **`NoIdentityInPasswordProvider`** - rejects passwords that contain the
  user's own username, email local-part, first name or last name
  (`org.keycloak.policy.PasswordPolicyProviderFactory`)
- **`NestedClaimToRoleMapper`** - maps a nested/array claim from an external
  identity provider's token into realm roles
  (`org.keycloak.broker.provider.IdentityProviderMapper`)

Each provider is registered the way Keycloak's SPI mechanism expects: a
`META-INF/services` file per provider type, pointing at the factory (or, for
the IdP mapper, the provider class itself).

## Prerequisites

- Java 21 and Maven 3.9+ (or just use the included `./mvnw`)
- Docker, for `docker compose up` and for the Testcontainers integration test

## Build and test

```bash
./mvnw clean verify
```

Runs the unit tests (`NoIdentityInPasswordProviderTest`), packages the JAR,
then runs the Testcontainers integration test (`AuthenticatorIT`), which
starts a real Keycloak 26.0.7 container with this JAR mounted and asserts all
three providers appear in `/admin/serverinfo`. The integration test needs a
working Docker daemon; if none is reachable it will fail at container
startup, not silently skip.

## Run it locally

```bash
mvn package   # or ./mvnw package - the compose file expects target/keycloak-spi-examples.jar
docker compose up
```

This starts Keycloak 26.0.7 with the built JAR mounted into `providers/`, a
`DEBUG` log category for `com.onloadcode.keycloak` so you can watch the
providers execute, and `keycloak/realm-export.json` auto-imported via
`--import-realm` - so you can try the authenticator without configuring a
realm by hand.

The imported `spi-examples` realm has:

- A `flagged-user` (password `Winter-Orbit-7729!`) with `requiresReview=true`,
  wired through a copy of the browser flow (`browser-with-review`) with the
  Review Confirmation step added as `REQUIRED` after the password form.
- A `normal-user` (password `Copper-River-4415!`) with no `requiresReview`
  attribute, who logs in through the same flow without seeing the extra step.
- The realm password policy set to `length(8) and noIdentityInPassword`, so
  registering a new user (or changing a password) rejects anything containing
  the user's name, username or email.
- A public client, `spi-examples-client`, for exercising the login flow.

Once it's up:

```bash
# Admin console: http://localhost:8080  (admin / admin)

TOKEN=$(curl -s -X POST http://localhost:8080/realms/master/protocol/openid-connect/token \
  -d 'client_id=admin-cli' -d 'grant_type=password' \
  -d 'username=admin' -d 'password=admin' | jq -r .access_token)

curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/admin/serverinfo | jq '.providers.authenticator, .providers."password-policy", .providers."identity-provider-mapper"'
```

Each of `review-confirm-authenticator`, `noIdentityInPassword` and
`nested-claim-to-role-mapper` should be in the list.

> The Quarkus distribution needs a build step before a dropped-in provider
> JAR takes effect. `start-dev` runs this automatically when it detects the
> JAR has changed, so a plain `docker compose up` is enough here - you don't
> need to run `kc.sh build` yourself for local development.

## Project layout

```
src/main/java/com/onloadcode/keycloak/
  authenticator/   ReviewConfirmAuthenticator(Factory)
  policy/          NoIdentityInPasswordProvider(Factory)
  mapper/          NestedClaimToRoleMapper
src/main/resources/
  META-INF/services/            SPI registration files
  theme-resources/templates/    review-confirm.ftl
  theme-resources/messages/     messages_en.properties
src/test/java/com/onloadcode/keycloak/
  policy/          unit tests for the password policy
  it/              Testcontainers integration test
keycloak/
  realm-export.json  imported by docker-compose.yml
```

`review-confirm.ftl` and its messages live under `theme-resources/`, not a
full custom theme: Keycloak auto-merges any `theme-resources/templates` and
`theme-resources/messages` shipped in a provider JAR into whichever login
theme the realm already uses, so the authenticator works without asking
anyone to also configure a custom theme.

## License

MIT - see [LICENSE](LICENSE).
