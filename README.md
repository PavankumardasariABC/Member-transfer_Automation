# Member-transfer_Automation

Standalone **Gradle + TestNG** end-to-end automation for **eAPI agreement creation** (and scaffolding for **member transfer**). This repository is intentionally **separate** from the large `dt2rcm_automation` suite so agreement flows can be run, extended, and CI-published independently.

Repository: [https://github.com/PavankumardasariABC/Member-transfer_Automation](https://github.com/PavankumardasariABC/Member-transfer_Automation)

## Prerequisites

- **JDK 17**
- Network access to your eAPI environment
- Valid **eAPI application credentials** (same class of headers as internal automation: `app_id`, `app_key`, `Authorization`)

## Security

Do **not** commit real `app_id`, `app_key`, or `Authorization` values to this (or any) **public** repository. Use environment variables or a private `gradle.properties` that stays **gitignored**.

## Configuration

### Environment variables (recommended)

| Variable | Purpose |
|----------|---------|
| `EAPI_BASE_URL` | eAPI root URL, e.g. `https://eapi.dev.abcfitness.net` |
| `EAPI_APP_ID` | `app_id` header |
| `EAPI_APP_KEY` | `app_key` header |
| `EAPI_AUTHORIZATION` | `Authorization` header value (e.g. `Basic …`) |
| `E2E_CLUB_NUMBER` | Club number to create the agreement in (default `06060` if unset) |
| `E2E_PAYMENT_PLAN` | Plan name as returned by eAPI `Get All Plans` (default `INSTALLMENT`) |

### Gradle properties (optional)

Copy `gradle.properties.example` to `gradle.properties` (ignored by git) and set:

- `e2e.eapi.baseUrl`
- `e2e.clubNumber`
- `e2e.paymentPlanName`

You must still provide **`EAPI_*` credentials via environment variables** — the build does not read secrets from `gradle.properties` by default.

## Run agreement E2E

```bash
export EAPI_BASE_URL='https://eapi.dev.abcfitness.net'
export EAPI_APP_ID='...'
export EAPI_APP_KEY='...'
export EAPI_AUTHORIZATION='...'
export E2E_CLUB_NUMBER='06060'
export E2E_PAYMENT_PLAN='INSTALLMENT'

./gradlew test --tests com.membertransfer.e2e.eapi.CreateAgreementE2ETest
```

Or pass the non-secret JVM properties inline:

```bash
./gradlew test \
  -De2e.eapi.baseUrl=https://eapi.dev.abcfitness.net \
  -De2e.clubNumber=12070 \
  -De2e.paymentPlanName=INSTALLMENT \
  --tests com.membertransfer.e2e.eapi.CreateAgreementE2ETest
```

(On Windows, set the same variables in your shell or CI job before invoking Gradle.)

## What the agreement test does

1. Calls **Get All Plans** for the club and resolves `paymentPlanId` by plan name.
2. Calls **Get Payment Plan Info** for that plan and reads `planValidationHash`.
3. Builds a **Create Agreement** JSON payload with synthetic contact + draft Visa billing data (Luhn-valid PAN).
4. Posts **Create Agreement**, asserts status message `success`.
5. Calls **Get Member Info** for the returned `memberId` and prints identifiers to the console.

## Member transfer (scaffold)

- **Client**: `com.membertransfer.e2e.eapi.EApiMemberTransferClient` — wraps `PUT …/members/{memberId}/agreements/transfer`.
- **Placeholder test**: `MemberTransferE2ETest` is **disabled** by default. Copy the pattern from your main automation project, set env vars for source/target clubs and `memberId`, then enable the test.

## Project layout

```
src/main/java/com/membertransfer/e2e/
  config/          # URL + header resolution
  eapi/            # REST clients
  http/            # RestAssured base configuration
  model/           # Request/response DTOs
  support/         # Agreement payload builders + test data

src/test/java/com/membertransfer/e2e/eapi/
  CreateAgreementE2ETest.java
  MemberTransferE2ETest.java   # disabled scaffold
```

## Relationship to `dt2rcm_automation`

This repo **reimplements a minimal slice** of the internal API models and HTTP calls used for agreement creation, so it can evolve on its own. When internal DTOs or headers change, update the corresponding classes here or consider publishing a shared internal artifact later.
