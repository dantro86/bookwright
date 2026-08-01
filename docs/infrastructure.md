# Infrastructure profiles

## Local demo

Use the launcher instead of invoking Compose and Gradle separately:

```bash
./scripts/run-local-tests.sh
./scripts/run-local-tests.sh --tests "io.bookwright.tests.db.*"
./scripts/run-local-tests.sh integrationTest
```

The launcher creates a uniquely named Compose project, waits for every service health check, discovers the
randomly published API and SSH ports, and forwards them to the Gradle test worker. JSch asks the operating system
for a free local tunnel port. An EXIT trap removes containers, networks, and volumes on success or failure.

The local Java booking application belongs to the opt-in Compose `integrated` profile. The launcher enables it
for `integrationTest` and the complete `test` task, then discovers and forwards its random host port just like the
restful-booker and SSH ports. Focused API and DB tasks skip the application image build.

The integrated application also owns the demo user lifecycle. At startup it creates one configured existing user
with a PBKDF2 password hash. Tests may register isolated users through `POST /api/users`, obtain one-hour sessions
through `POST /api/auth/sessions`, and delete their own users through an authenticated API call. Session tokens are
stored only as SHA-256 hashes. The protected `/app/bookings` page accepts the same HTTP-only session cookie that the
fixture injects into Playwright.

Local passwords are published demo values. The validator permits this profile only when all of these conditions
hold: `STAND=local`, `ssh.auth.mode=PASSWORD`, host-key checking is disabled, and the SSH host is loopback.

## Non-local DB over SSH

Non-local environments must supply their own DB settings and SSH trust material. The `prod` stand deliberately
contains none of them.

```bash
export DB_PASSWORD="..."
export SSH_KEY_PASSPHRASE="..." # omit when the key is not encrypted

./gradlew test \
  -DSTAND=staging \
  -Ddb.host=mysql.internal.example \
  -Ddb.name=hotel \
  -Ddb.user=qa_runner \
  -Dssh.host=bastion.example \
  -Dssh.user=qa_runner \
  -Dssh.auth.mode=PRIVATE_KEY \
  -Dssh.private.key.path=/secure/path/id_ed25519 \
  -Dssh.known.hosts.path=/secure/path/known_hosts \
  --tests "io.bookwright.tests.db.*"
```

Strict host-key checking defaults to `true`; the validator rejects a private-key profile without readable key and
`known_hosts` files. Ports default to SSH `22` and MySQL `3306`, while the local forwarding port defaults to `0`
(dynamic). Override them with `-Dssh.port`, `-Ddb.port`, or `-Ddb.tunnel.port` when required.

## Updating container pins

Image references in `docker/docker-compose.yml` include immutable manifest digests. An update is an explicit
dependency change: inspect the new multi-architecture manifest, replace both the readable version tag (when the
publisher provides one) and digest, run the full launcher, and review the resulting health and compatibility checks.
