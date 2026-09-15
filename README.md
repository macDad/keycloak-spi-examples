# Keycloak SPI Examples

![Keycloak](https://img.shields.io/badge/Keycloak-26.0.7-4D96D9?style=flat-square&logo=keycloak&logoColor=white) ![Java](https://img.shields.io/badge/Java-21-E8833A?style=flat-square&logo=openjdk&logoColor=white) ![Maven](https://img.shields.io/badge/Maven-3.9+-1B2838?style=flat-square&logo=apachemaven&logoColor=white) ![Tests](https://img.shields.io/badge/tests-JUnit%205%20%2B%20Testcontainers-3FA66B?style=flat-square) ![License](https://img.shields.io/badge/license-MIT-C9D2DB?style=flat-square)

Production-shaped custom extensions for Keycloak 26 — authenticator, password policy provider, and identity provider mapper.

## What this is

Three working Keycloak SPIs, each solving a requirement the admin console can't express on its own: a login step that only applies to flagged accounts, a password rule the built-in policies don't cover, and a claim shape the built-in IdP mappers can't reach. Each one is a real, registered provider — not a snippet — with unit tests and an integration test that boots a real Keycloak container.

Companion code for [Keycloak Custom SPIs: Authenticators, Password Policies and IdP Mappers](ARTICLE_URL_PLACEHOLDER) on OnloadCode. The article is the long-form explanation; this README is the reference.

## The three extensions

| Extension | What it does | SPI |
|---|---|---|
| Review Confirmation Authenticator | Adds a confirmation screen for accounts flagged with `requiresReview=true`, then clears the flag once confirmed. | `org.keycloak.authentication.AuthenticatorFactory` |
| No-Identity Password Policy | Rejects passwords containing the user's username, email local-part, first name, or last name. | `org.keycloak.policy.PasswordPolicyProviderFactory` |
| Nested Claim to Role Mapper | Maps a nested or array claim from a brokered OIDC token (e.g. `resource.access.fleet.roles`) into realm roles. | `org.keycloak.broker.provider.IdentityProviderMapper` |

## Quick start

Clone the repo:

```bash
git clone https://github.com/macDad/keycloak-spi-examples.git
cd keycloak-spi-examples
```

Build the provider JAR:

```bash
./mvnw clean package
```

Start Keycloak with it mounted in:

```bash
docker compose up
```

> [!NOTE]
> The Quarkus distribution needs a build step before a dropped-in provider JAR takes effect. `start-dev` runs that build automatically when it detects the JAR changed, so `docker compose up` alone is enough here. A non-dev deployment needs an explicit `kc.sh build`.

Once it's running, open `http://localhost:8080`, sign in to the admin console with `admin` / `admin`, and:

- Check **Authentication → Flows** for `browser-with-review` — the browser flow copy with the Review Confirmation step added.
- Check **Realm Settings → Authentication → Policies → Password Policy** for `Not Containing Identity`.
- Log in as `flagged-user` / `Winter-Orbit-7729!` and you'll hit the confirmation screen; log in as `normal-user` / `Copper-River-4415!` and you won't. Both are pre-created by the realm import.

## Project structure

```text
src/main/java/com/onloadcode/keycloak/
├── authenticator/
│   ├── ReviewConfirmAuthenticator.java
│   └── ReviewConfirmAuthenticatorFactory.java
├── policy/
│   ├── NoIdentityInPasswordProvider.java
│   └── NoIdentityInPasswordProviderFactory.java
└── mapper/
    └── NestedClaimToRoleMapper.java

src/main/resources/
├── META-INF/services/
│   ├── org.keycloak.authentication.AuthenticatorFactory     # registers the authenticator factory
│   ├── org.keycloak.policy.PasswordPolicyProviderFactory    # registers the password policy factory
│   └── org.keycloak.broker.provider.IdentityProviderMapper  # registers the mapper class directly (no separate factory for this SPI)
└── theme-resources/
    ├── templates/review-confirm.ftl        # auto-merged into whichever login theme is active
    └── messages/messages_en.properties     # message keys read by the template and the password policy

src/test/java/com/onloadcode/keycloak/
├── policy/NoIdentityInPasswordProviderTest.java  # unit tests, no Keycloak server involved
└── it/AuthenticatorIT.java                       # Testcontainers integration test

keycloak/realm-export.json   # imported automatically by docker-compose.yml
docker-compose.yml           # Keycloak 26.0.7, JAR mounted into providers/
```

## Review Confirmation Authenticator

**Requirement:** accounts carrying a `requiresReview` attribute must confirm an extra screen before finishing login; everyone else logs in normally.

**Classes:** `ReviewConfirmAuthenticator`, `ReviewConfirmAuthenticatorFactory`.

**Enable it:** copy the built-in **browser** flow (built-in flows can't be edited directly), add an execution for **Review Confirmation** as `REQUIRED` after the password step, then bind the copy as the realm's browser flow. The bundled realm export already does this as `browser-with-review`.

> [!WARNING]
> Use `failureChallenge()` for a user mistake, not `failure()`. `failure()` aborts the whole flow with a generic error page; `failureChallenge()` re-renders the form with a message, which is what the user actually needs.

> [!NOTE]
> `getId()` on the factory is permanent once a realm uses it — it's written into that realm's flow config. Renaming it orphans the execution in every realm that already has it.

## No-Identity Password Policy

**Requirement:** reject passwords containing the username, the email local-part, or the first/last name — a common audit finding the built-in policies (length, digits, reuse, HIBP) don't cover.

**Classes:** `NoIdentityInPasswordProvider`, `NoIdentityInPasswordProviderFactory`.

**Enable it:** **Realm Settings → Authentication → Policies → Password Policy**, add **Not Containing Identity**. The bundled realm sets this via `passwordPolicy: "length(8) and noIdentityInPassword"`.

> [!WARNING]
> Returning `null` from `validate()` means the password *passed*; returning a `PolicyError` means it failed. It reads backwards the first time — get it wrong and the policy silently accepts everything.

## Nested Claim to Role Mapper

**Requirement:** an enterprise IdP sends roles inside a nested array (e.g. `resource.access.fleet.roles`) that the built-in flat-claim mappers can't reach.

**Classes:** `NestedClaimToRoleMapper`.

**Enable it:** under an identity provider's **Mappers** tab, add **Nested Claim To Role**, and set the `claim.path` and `role.prefix` config values. Not part of the bundled realm export — it needs a real or mock OIDC identity provider to attach to, which is outside the scope of the quick-start realm. You can still confirm it's registered without one: it's in the **Mapper type** dropdown on any OIDC identity provider's Mappers tab, and under `providers["identity-provider-mapper"]` in `/admin/serverinfo`.

> [!WARNING]
> Implement both `importNewUser()` and `updateBrokeredUser()`. Implementing only `importNewUser()` is the single most common mapper bug: roles apply on first login and then never update again, which usually surfaces months later as access that should have been revoked and wasn't.

## Running the tests

```bash
./mvnw test
```

Runs the unit tests only (`NoIdentityInPasswordProviderTest`) — no Docker, no Keycloak server, done in under a second.

```bash
./mvnw verify
```

Also runs `AuthenticatorIT`, which starts a real Keycloak 26.0.7 container with this JAR mounted via Testcontainers and asserts all three providers appear in `/admin/serverinfo`. Needs a working Docker daemon. Expect well under a minute once the Keycloak image is cached locally; the first run also pays for the image pull.

## Compatibility

| Keycloak | Branch/Tag | Status |
|---|---|---|
| 26.0.x | `main` | Supported |

The Keycloak version is pinned in one place: the `keycloak.version` property in `pom.xml`. Bumping it is the first step of an upgrade — see the article for the full sequence.

## Contributing

Issues and PRs are welcome.
Keep examples minimal and runnable — if it can't be built and exercised with `docker compose up`, it doesn't belong in this repo.
Run `./mvnw verify` before opening a PR.

## License

MIT — see [LICENSE](LICENSE).
