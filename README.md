# Permissions

Internal service (not exposed by nginx) that stores snippet permissions and searches users to share with.

## Users search (`GET /users?search=&limit=`)

Returns `[{id, name, email}]`. `id` is the Auth0 `user_id` (the JWT `sub`), `name` falls back to nickname and then email. Search is a prefix match over name, email and nickname (Auth0 v3 needs at least 3 characters for a wildcard; shorter terms match exactly). A blank search returns the first `limit` users (max 100).

| Variable | Required | Description |
|---|---|---|
| `USERS_PROVIDER` | no (default `none`) | `auth0` searches Auth0; `none` returns an empty list and logs a warning at startup. |
| `AUTH0_DOMAIN` | with `auth0` | Tenant domain, e.g. `tenant.us.auth0.com`. |
| `AUTH0_MGMT_CLIENT_ID` | with `auth0` | Machine to Machine app authorized on the Management API with scope `read:users`. |
| `AUTH0_MGMT_CLIENT_SECRET` | with `auth0` | Secret of that app. Never logged or returned. |

With `USERS_PROVIDER=auth0` the service fails to start if any of the three Auth0 variables is missing. Auth0 failures are returned as `502` with `{"error": "USERS_PROVIDER_ERROR", ...}`.

Other variables: `PORT`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

## Quality gates

`./gradlew check` runs Spotless, Checkstyle, tests and a JaCoCo gate of at least 80% line coverage.
