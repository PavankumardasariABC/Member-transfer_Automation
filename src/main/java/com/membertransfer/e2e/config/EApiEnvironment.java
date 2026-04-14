package com.membertransfer.e2e.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Resolves eAPI base URL and auth headers from system properties, environment variables,
 * or Gradle {@code -P} properties (forwarded as system properties from {@code build.gradle}).
 * <p>
 * Base URL resolution order:
 * <ol>
 *   <li>{@code e2e.eapi.baseUrl} system property</li>
 *   <li>{@code EAPI_BASE_URL} environment variable</li>
 *   <li>{@code e2e.envProfile} / {@code E2E_ENV_PROFILE} → {@code e2e/environments.json} → {@code eapiBaseUrl}</li>
 * </ol>
 * <p>
 * Required for a successful run:
 * <ul>
 *   <li>One of: explicit base URL <em>or</em> a known environment profile id</li>
 *   <li>{@code EAPI_APP_ID}, {@code EAPI_APP_KEY}, {@code EAPI_AUTHORIZATION} — same semantics as the
 *       internal automation headers (never commit real values to a public repository)</li>
 * </ul>
 */
public final class EApiEnvironment {

    public static final String PROP_BASE_URL = "e2e.eapi.baseUrl";
    public static final String ENV_BASE_URL = "EAPI_BASE_URL";

    public static final String PROP_ENV_PROFILE = E2eCatalog.PROP_ENV_PROFILE;
    public static final String ENV_ENV_PROFILE = E2eCatalog.ENV_ENV_PROFILE;

    public static final String ENV_APP_ID = "EAPI_APP_ID";
    public static final String ENV_APP_KEY = "EAPI_APP_KEY";
    public static final String ENV_AUTHORIZATION = "EAPI_AUTHORIZATION";

    private EApiEnvironment() {
    }

    public static String baseUrl() {
        String v = firstNonBlank(
                System.getProperty(PROP_BASE_URL),
                System.getenv(ENV_BASE_URL)
        );
        if (v == null || v.isBlank()) {
            String profileId = envProfileId();
            if (profileId != null && !profileId.isBlank()) {
                v = E2eCatalog.profile(profileId)
                        .map(EnvironmentProfile::getEapiBaseUrl)
                        .orElse(null);
                if (v == null || v.isBlank()) {
                    throw new IllegalStateException("Unknown E2E environment profile '" + profileId
                            + "'. Valid ids: " + E2eCatalog.profileIdsForMessage());
                }
            }
        }
        if (v == null || v.isBlank()) {
            throw new IllegalStateException(
                    "Set eAPI base URL via -D" + PROP_BASE_URL + "=... or " + ENV_BASE_URL
                            + ", or set profile via -D" + PROP_ENV_PROFILE + "=... / " + ENV_ENV_PROFILE
                            + " (see src/main/resources/e2e/environments.json). Known profiles: "
                            + E2eCatalog.profileIdsForMessage());
        }
        return v.replaceAll("/$", "");
    }

    /**
     * Optional profile id from {@code e2e/environments.json} (e.g. {@code qa-eapi-dev}).
     */
    public static String envProfileId() {
        return firstNonBlank(
                System.getProperty(PROP_ENV_PROFILE),
                System.getenv(ENV_ENV_PROFILE)
        );
    }

    /**
     * Logs resolved stack + club metadata to stdout (safe for CI logs).
     */
    public static void logRuntimeContext() {
        String profileId = envProfileId();
        if (profileId != null && !profileId.isBlank()) {
            E2eCatalog.profile(profileId).ifPresentOrElse(
                    p -> E2eCatalog.printEnvironmentContext(profileId, p),
                    () -> System.out.println("--- Unknown environment profile id: " + profileId + " ---")
            );
        } else {
            System.out.println("--- No E2E_ENV_PROFILE set (using explicit eAPI base URL) ---");
        }
        E2eCatalog.printClubContext(clubNumber());
    }

    public static Map<String, String> authHeaders() {
        // Prefer JVM system properties (Gradle forwards secrets as -D for reliable TestNG workers), then OS env.
        String appId = firstNonBlank(System.getProperty(ENV_APP_ID), System.getenv(ENV_APP_ID));
        String appKey = firstNonBlank(System.getProperty(ENV_APP_KEY), System.getenv(ENV_APP_KEY));
        String authorization = firstNonBlank(System.getProperty(ENV_AUTHORIZATION), System.getenv(ENV_AUTHORIZATION));
        if (appId == null || appKey == null || authorization == null) {
            String miss = "";
            if (appId == null) {
                miss += " " + ENV_APP_ID + " is blank/missing;";
            }
            if (appKey == null) {
                miss += " " + ENV_APP_KEY + " is blank/missing;";
            }
            if (authorization == null) {
                miss += " " + ENV_AUTHORIZATION + " is blank/missing;";
            }
            throw new IllegalStateException(
                    "eAPI credentials are required." + miss
                            + " Add repository secrets " + ENV_APP_ID + ", " + ENV_APP_KEY + ", " + ENV_AUTHORIZATION
                            + " (GitHub → Settings → Secrets and variables → Actions) and ensure this workflow passes them in `env:`.");
        }
        Map<String, String> m = new HashMap<>();
        m.put("app_id", appId);
        m.put("app_key", appKey);
        m.put("Authorization", authorization);
        return Map.copyOf(m);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }

    public static String clubNumber() {
        return Objects.requireNonNullElse(
                firstNonBlank(System.getProperty("e2e.clubNumber"), System.getenv("E2E_CLUB_NUMBER")),
                "06060");
    }

    public static String paymentPlanName() {
        return Objects.requireNonNullElse(
                firstNonBlank(System.getProperty("e2e.paymentPlanName"), System.getenv("E2E_PAYMENT_PLAN")),
                "INSTALLMENT");
    }

    public static String formatClubNumber(String clubNumber) {
        if (clubNumber == null) {
            return null;
        }
        return clubNumber.replaceFirst("^0(?=.{4,})", "");
    }
}
