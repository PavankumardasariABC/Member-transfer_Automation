# Member-transfer_Automation

Standalone **Gradle + TestNG** end-to-end automation for **eAPI agreement creation** (and scaffolding for **member transfer**). This repository is intentionally **separate** from the large `dt2rcm_automation` suite so agreement flows can be run, extended, and CI-published independently.

Repository: [https://github.com/PavankumardasariABC/Member-transfer_Automation](https://github.com/PavankumardasariABC/Member-transfer_Automation)

### CI when you do **not** have self-hosted runners

Many internal eAPI URLs **do not resolve** from GitHub’s **hosted** runners (`ubuntu-latest`). That is normal—not a bug in this repo.

| Workflow | When it runs | Needs eAPI / secrets? |
|----------|----------------|------------------------|
| [**Gradle build**](.github/workflows/gradle-build.yml) | Every **push** / **pull request** to `main` or `master` | **No** — only `compileJava` / `compileTestJava`. Use this as your default **green CI** for the team. |
| [**eAPI Create Agreement E2E**](.github/workflows/eapi-create-agreement.yml) | **Manual** (workflow_dispatch) | **Yes** — secrets + network to eAPI. On hosted runners, internal DNS usually **fails** until your org adds **self-hosted** runners or a **publicly resolvable** eAPI host. |

**Practical setup for a new team repo:** enable the **Gradle build** workflow so PRs stay healthy. Add the three **Actions secrets** for anyone who runs the manual **eAPI Create Agreement E2E** workflow. If eAPI hostnames are internal-only, that workflow may still fail on `ubuntu-latest` from **DNS/network** — run the same Gradle test **locally** or on **internal CI** in that case.

## Prerequisites

- **JDK 17**
- Network access to your eAPI environment
- Valid **eAPI application credentials** (same class of headers as internal automation: `app_id`, `app_key`, `Authorization`)

## Security

Do **not** commit real `app_id`, `app_key`, or `Authorization` values to this (or any) **public** repository. Use **GitHub Actions secrets**, **GitHub Environment secrets**, environment variables in your shell, or a private `gradle.properties` that stays **gitignored**.

## Runtime configuration (JSON catalogs)

Non-secret **stack** and **club** metadata live under `src/main/resources/e2e/`:

| File | Purpose |
|------|---------|
| [`e2e/environments.json`](src/main/resources/e2e/environments.json) | Named profiles (`qa-eapi-dev`, `staging`, `beta`) → `eapiBaseUrl`, optional `rcmBillingUrl` / `commerceUiUrl` for verification links. |
| [`e2e/clubs.json`](src/main/resources/e2e/clubs.json) | Automation clubs: `organizationId`, `locationId`, `links` (Commerce URLs, notes). Used for **log output** and optional **strict CI** checks. |

### Choosing the eAPI URL at run time

**Option A — named profile (recommended for CI and GitHub Actions):**

- Set `E2E_ENV_PROFILE` to a key in `environments.json` (e.g. `qa-eapi-dev`).

**Option B — explicit URL (overrides profile):**

- Set `EAPI_BASE_URL` or `-De2e.eapi.baseUrl=...`.

Resolution order: `e2e.eapi.baseUrl` → `EAPI_BASE_URL` → profile from `E2E_ENV_PROFILE` / `e2e.envProfile`.

### Adding a new club

1. Open [`src/main/resources/e2e/clubs.json`](src/main/resources/e2e/clubs.json).
2. Append an object with at least `clubNumber` (5-digit string).
3. Fill `organizationName`, `organizationId`, `locationName`, `locationId` from your org catalog (same fields you use in `dt_clubs.json` / Commerce).
4. Under `links`, add any URLs your team uses for verification (Commerce members screen, internal wiki, DataTrak notes, etc.). Keys are free-form strings; the test prints all entries.

Optional: point the suite at a **custom** clubs file (same JSON shape) with:

- `E2E_CLUBS_JSON_PATH=/absolute/path/to/clubs.json`, or  
- `-De2e.clubsJsonPath=/absolute/path/to/clubs.json`

### Strict catalog check (CI)

If the workflow input **Require club catalog** is `true`, or you set `E2E_REQUIRE_CLUB_CATALOG=true` / `-De2e.requireClubInCatalog=true`, the test **fails** when the selected club is **not** listed in `clubs.json`. Use this to catch typos in `club_number` early.

## Environment variables

| Variable | Purpose |
|----------|---------|
| `EAPI_BASE_URL` | eAPI root URL (optional if `E2E_ENV_PROFILE` is set) |
| `E2E_ENV_PROFILE` | Profile id from `e2e/environments.json` (e.g. `qa-eapi-dev`) |
| `EAPI_APP_ID` | `app_id` header |
| `EAPI_APP_KEY` | `app_key` header |
| `EAPI_AUTHORIZATION` | `Authorization` header (e.g. `Basic …`) |
| `E2E_CLUB_NUMBER` | Club number (default `06060`) |
| `E2E_PAYMENT_PLAN` | Plan name from **Get All Plans** (default `INSTALLMENT`) |
| `E2E_CLUBS_JSON_PATH` | Optional absolute path to alternate `clubs.json` |
| `E2E_REQUIRE_CLUB_CATALOG` | `true` to require club in `clubs.json` |
| `E2E_TOTAL_AGREEMENTS` | Total agreements to create on this run (default `1`, max `100`) |
| `E2E_SHARD_COUNT` | How many parallel shards (matrix size); default `1` (single process creates all) |
| `E2E_SHARD_INDEX` | Zero-based shard id in `[0, E2E_SHARD_COUNT)`; default `0` |
| `E2E_AGREEMENT_QUEUE_TIMEOUT_MINUTES` | Max minutes to poll **Get Member Info** until `currentQueue` is **Posted** (default **40**, same as dt2rcm `ApiAwaitUtils` for agreements). JVM override: `-De2e.agreementQueueTimeoutMinutes=…` |

## Gradle properties (optional)

Copy [`gradle.properties.example`](gradle.properties.example) to `gradle.properties` (gitignored). You can set `e2e.envProfile` and optionally `e2e.eapi.baseUrl` to override the profile URL. Prefer **environment variables** (`E2E_CLUB_NUMBER`, `E2E_PAYMENT_PLAN`, …) for club/plan in CI — setting `e2e.clubNumber` / `e2e.paymentPlanName` in `gradle.properties` would override those env vars in the test JVM.

Credentials should still come from **environment variables** or CI secrets.

## Run agreement E2E (local)

**Using a named profile:**

```bash
export E2E_ENV_PROFILE='qa-eapi-dev'
export EAPI_APP_ID='...'
export EAPI_APP_KEY='...'
export EAPI_AUTHORIZATION='...'
export E2E_CLUB_NUMBER='06060'
export E2E_PAYMENT_PLAN='INSTALLMENT'

./gradlew test --tests com.membertransfer.e2e.eapi.CreateAgreementE2ETest
```

**Using an explicit eAPI URL:**

```bash
export EAPI_BASE_URL='https://eapi.dev.abcfitness.net'
export EAPI_APP_ID='...'
export EAPI_APP_KEY='...'
export EAPI_AUTHORIZATION='...'
export E2E_CLUB_NUMBER='12070'
export E2E_PAYMENT_PLAN='INSTALLMENT'

./gradlew test --tests com.membertransfer.e2e.eapi.CreateAgreementE2ETest
```

## GitHub Actions

Workflow: [`.github/workflows/eapi-create-agreement.yml`](.github/workflows/eapi-create-agreement.yml) (**workflow_dispatch**).

### One-time setup: repository secrets (required)

The workflow **cannot** read eAPI credentials from this repository; they are **not** in git (and must never be). You must create **three** [Actions secrets](https://docs.github.com/en/actions/security-guides/using-secrets-in-github-actions) on **this same GitHub repository** that runs the workflow (not only on your laptop).

1. Open **your repo** on GitHub → **Settings** → **Secrets and variables** → **Actions**.
2. Click **New repository secret** three times and add exactly these names (case-sensitive):

| Name | Value to paste |
|------|----------------|
| `EAPI_APP_ID` | Same value you use as the eAPI **`app_id`** header (often from your internal automation / app registration). |
| `EAPI_APP_KEY` | Same value as the eAPI **`app_key`** header. |
| `EAPI_AUTHORIZATION` | Full **`Authorization`** header value (e.g. `Basic …` or `Bearer …`), **not** the word “Bearer” alone. |

**Important — GitHub secret *names* are not Java constant names.** If you copied values from `dt2rcm`’s `EApiHelper.java`, you must still name the secrets **`EAPI_APP_ID`**, **`EAPI_APP_KEY`**, and **`EAPI_AUTHORIZATION`**. Names like `APP_ID_WITH_AUTH`, `APP_KEY_WITH_AUTH`, or `BASIC` will **not** be read by this workflow (you will see “empty or unset” for `EAPI_*`).

3. Save each secret, then re-run **Actions** → **eAPI Create Agreement E2E** → **Run workflow**.

**If the job fails with “Secret EAPI_APP_ID is empty or unset”:** GitHub is not supplying those secrets to the job. Typical causes: secrets were added on a **different** fork or organization repo; the workflow ran from a **pull request from a fork** (secrets are not passed to fork PRs); or the names have a typo (`EAPI_APP_ID` vs `EAPI_APPID`). Fix by adding the three secrets on the repository shown in the workflow run URL.

**Where do the values come from?** Use the **same** eAPI application credentials your team already uses for QA/dev eAPI (for example the values you export locally as `EAPI_APP_ID`, `EAPI_APP_KEY`, `EAPI_AUTHORIZATION` in the commands above). Copy them from your password manager or internal docs — **do not** paste them into a commit or issue.

### Use the exact same values as a working local run

The workflow already injects **`EAPI_APP_ID`**, **`EAPI_APP_KEY`**, and **`EAPI_AUTHORIZATION`** into the job using the **same names** your shell uses. You are not changing values—only **copying** them from where they work locally into **GitHub → Settings → Secrets and variables → Actions** (repository secrets). **Never** commit those strings into this repo.

1. On your machine, find the three strings that make local `./gradlew test --tests com.membertransfer.e2e.eapi.CreateAgreementE2ETest` succeed. Typical places:
   - **Terminal:** `export EAPI_APP_ID=…` (and the other two) in `~/.zshrc`, `~/.bash_profile`, or a script you run before Gradle.
   - **`gradle.properties`** in the project root (often **gitignored**—that is correct; keep it local only).
   - **IDE run configuration:** Environment variables on the TestNG / Gradle run.
2. In GitHub: **Settings → Secrets and variables → Actions → New repository secret** (three times).
3. For each row below, create a secret whose **name** matches the left column and whose **value** is the **exact same string** you use locally for that name:

| GitHub secret name | Must match your local |
|--------------------|------------------------|
| `EAPI_APP_ID` | Same as local `EAPI_APP_ID` |
| `EAPI_APP_KEY` | Same as local `EAPI_APP_KEY` |
| `EAPI_AUTHORIZATION` | Same as local `EAPI_AUTHORIZATION` (full header value, including any `Basic ` / `Bearer ` prefix if you use it locally) |

4. Save all three, then run the workflow from **Actions** (not from a fork PR unless your org allows secrets there).

**Sanity check (local, does not print secrets):**

```bash
for n in EAPI_APP_ID EAPI_APP_KEY EAPI_AUTHORIZATION; do
  if [ -n "${!n}" ]; then echo "$n=set"; else echo "$n=MISSING"; fi
done
```

If all three show `set` locally but GitHub still reports empty secrets, the secrets were almost certainly added on a **different** GitHub repository or account than the one running the workflow—open the run URL and add secrets on **that** repo’s Settings page.

### Same eAPI headers as `dt2rcm_automation` (Create Agreement)

In **dt2rcm_automation**, `CreateAgreementTest` calls `eApiFactory.createAgreement`, which uses **`HEADERS_WITH_AUTH`** from `api/src/main/java/com/abcfinancial/api/apps/eapi/EApiHelper.java` (`app_id`, `app_key`, `Authorization`).

Copy the **string literals** from that file into GitHub secrets (or into your shell `export`s) using this mapping:

| GitHub secret **name** (required) | Copy **value** from dt2rcm `EApiHelper` constant |
|-----------------------------------|--------------------------------------------------|
| `EAPI_APP_ID` | `APP_ID_WITH_AUTH` |
| `EAPI_APP_KEY` | `APP_KEY_WITH_AUTH` |
| `EAPI_AUTHORIZATION` | `BASIC` (entire `Authorization` header value, including the `Basic ` prefix) |

The **left column** is what you type in GitHub’s **Name** field; the **right column** is only where the string comes from in Java.

**Run the dt2rcm agreement test locally** (from the **dt2rcm_automation** repo root; uses QA eAPI URL from `obc/src/test/resources/services.json` and dev club `06060`, aligned with Member-transfer profile **`qa-eapi-dev`**):

```bash
cd /path/to/dt2rcm_automation
./gradlew :obc:test --tests com.abcfinancial.test.eapi.agreement.CreateAgreementTest \
  -DenvType=qa -DreskinEnv=dev -DclubNumber=06060
```

For **staging** defaults (`config.properties` uses `stg` / `07038`), use `-DenvType=stg -DreskinEnv=stg -DclubNumber=07038` (and workflow profile **`staging`** in Member-transfer).

**Then run Member-transfer with the same three headers** (after exporting the same values you copied from `EApiHelper`):

```bash
cd /path/to/Member-transfer_Automation
export E2E_ENV_PROFILE='qa-eapi-dev'
export EAPI_APP_ID='…'        # same as APP_ID_WITH_AUTH
export EAPI_APP_KEY='…'      # same as APP_KEY_WITH_AUTH
export EAPI_AUTHORIZATION='…' # same as BASIC
./gradlew test --tests com.membertransfer.e2e.eapi.CreateAgreementE2ETest --no-daemon
```

**Security note:** dt2rcm currently embeds those values in source; Member-transfer expects them as **secrets** or local env so they are not committed to a public repo. Prefer rotating credentials if they were ever exposed.

**Inputs at run time:**

- **run_mode** — **`compile-only`** (default): `compileJava` / `compileTestJava` only — **no eAPI**, works on GitHub-hosted runners. **`agreement-e2e`**: runs `CreateAgreementE2ETest` with secrets; fails on `ubuntu-latest` with **`UnknownHostException`** if your eAPI hostname is not reachable from the public internet (internal DNS).
- **environment_profile** — choice: `qa-eapi-dev`, `staging`, `beta` (must exist in `environments.json`). Used when **run_mode** is `agreement-e2e`.
- **club_number** — e.g. `06060`.
- **payment_plan** — e.g. `INSTALLMENT`, `CASH`.
- **require_club_catalog** — boolean; if `true`, `club_number` must appear in `e2e/clubs.json`.
- **agreement_count** — how many agreements to create in **one** job (1–100), **sequentially** (same as local `E2E_TOTAL_AGREEMENTS` with a single shard).
- **test_class** — optional full TestNG class name; empty = default `CreateAgreementE2ETest`.

### Where to see results (QA run)

1. Open **Actions** → **eAPI Create Agreement E2E** → select your workflow run.
2. **Summary**: the **Results summary** step appends a markdown table from `agreements-shard-0.json` when present.
3. **Artifacts:** download **`agreement-results`** — contains `build/e2e-agreement-results/` (e.g. `agreements-shard-0.json`).

Locally, the same JSON is written under `build/e2e-agreement-results/` after `./gradlew test`.

**Troubleshooting — `IllegalStateException` when creating `EApiAgreementClient`:** the test JVM could not read `EAPI_APP_ID` / `EAPI_APP_KEY` / `EAPI_AUTHORIZATION`. Confirm the three **repository Action secrets** exist (exact names) and re-run. The Gradle build now copies these variables into the forked test process explicitly so GitHub Actions picks them up reliably.

**Troubleshooting — `java.net.UnknownHostException` on GitHub Actions:** the test is calling **`https://eapi.dev.abcfitness.net`** (for `qa-eapi-dev`). **`ubuntu-latest`** cannot resolve many internal `*.abcfitness.net` names. Either use workflow **run_mode → `compile-only`**, run **agreement-e2e** **locally** / on **internal CI** / **self-hosted** runners, or ask IT for a publicly resolvable eAPI host.

**Repository secrets** (Settings → Secrets and variables → Actions):

| Secret | Purpose |
|--------|---------|
| `EAPI_APP_ID` | `app_id` |
| `EAPI_APP_KEY` | `app_key` |
| `EAPI_AUTHORIZATION` | `Authorization` header value |

**Optional: GitHub Environments** (e.g. `qa`, `staging`) with the same secret names let you scope credentials per stack. To use them, edit the workflow job and add:

```yaml
jobs:
  agreement-e2e:
    runs-on: ubuntu-latest
    environment: qa   # name of your GitHub Environment
```

Then remove duplicate secrets from the repository level or keep repository secrets as fallback, depending on your org policy.

## What the agreement test does

Aligned with **dt2rcm_automation** `CreateAgreementTest` (create → success → wait **Posted** → member **Active** → **GET payment methods** + card/slot assertions):

1. Resolves **shard** settings (`E2E_TOTAL_AGREEMENTS`, `E2E_SHARD_COUNT`, `E2E_SHARD_INDEX`) and prints the resolved **environment profile** and **club catalog** entry.
2. For each agreement index on this shard: **Get All Plans** / **Get Payment Plan Info** → build **Create Agreement** (synthetic contact + draft Visa, Luhn-valid PAN) → POST create → assert status **success** → print `CREATED_AGREEMENT …`.
3. **Poll** **Get Member Info** until `currentQueue` is **Posted** (same wait semantics as dt2rcm `waitForAgreementHasQueueStatus`, default timeout 40 minutes, 15s poll).
4. Assert member **Active** and queue **Posted**; **GET** `…/wallets/paymentmethods` and assert names, last four, card type, exp, method type **Credit Card**, slots **CardOnFile** + **ClubBilling** (TestNG `SoftAssert`, then `assertAll`).
5. Append shard JSON + print `E2E OK …` line. With defaults (`total=1`, one shard), one full agreement run.

### Local multi-agreement (single machine, sequential)

```bash
export E2E_TOTAL_AGREEMENTS=5
# defaults: one shard → this JVM creates all 5 in sequence
./gradlew test --tests com.membertransfer.e2e.eapi.CreateAgreementE2ETest
```

## Member transfer (scaffold)

- **Client**: `com.membertransfer.e2e.eapi.EApiMemberTransferClient` — `PUT …/members/{memberId}/agreements/transfer`.
- **Placeholder test**: `MemberTransferE2ETest` is **disabled** by default.

## Project layout

```
src/main/resources/e2e/
  environments.json   # Named eAPI / Commerce stacks
  clubs.json          # Club + org + verification links

src/main/java/com/membertransfer/e2e/
  config/             # EApiEnvironment, E2eCatalog, E2eShardConfig
  constants/          # ApiRequestStatus, MemberStatus, QueueStatus, PaymentSlot, … (dt2rcm parity)
  apps/pg/            # CardBrand (dt2rcm parity)
  eapi/               # REST clients
  http/               # RestAssured base configuration
  model/              # Request/response DTOs (+ wallet PaymentMethodsResponse)
  support/            # Agreement builders, EApiAgreementAwait, test data

src/test/java/com/membertransfer/e2e/eapi/
  CreateAgreementE2ETest.java
  MemberTransferE2ETest.java   # disabled scaffold

.github/workflows/
  eapi-create-agreement.yml      # Manual agreement E2E
```

## Relationship to `dt2rcm_automation`

This repo **reimplements a minimal slice** of the internal API models and HTTP calls used for agreement creation, so it can evolve on its own. When internal DTOs or headers change, update the corresponding classes here or consider publishing a shared internal artifact later.
