package com.membertransfer.e2e.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Resolves eAPI base URL and auth headers from system properties, environment variables,
 * or Gradle {@code -P} properties (forwarded as system properties from {@code build.gradle}).
 * <p>
 * Required for a successful run:
 * <ul>
 *   <li>{@code e2e.eapi.baseUrl} — eAPI root, e.g. {@code https://eapi.dev.abcfitness.net}</li>
 *   <li>{@code EAPI_APP_ID}, {@code EAPI_APP_KEY}, {@code EAPI_AUTHORIZATION} — same semantics as the
 *       internal automation headers (never commit real values to a public repository)</li>
 * </ul>
 */
public final class EApiEnvironment {

    public static final String PROP_BASE_URL = "e2e.eapi.baseUrl";
    public static final String ENV_BASE_URL = "EAPI_BASE_URL";

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
            throw new IllegalStateException(
                    "Set eAPI base URL via -D" + PROP_BASE_URL + "=... or " + ENV_BASE_URL + " environment variable.");
        }
        return v.replaceAll("/$", "");
    }

    public static Map<String, String> authHeaders() {
        String appId = firstNonBlank(System.getenv(ENV_APP_ID));
        String appKey = firstNonBlank(System.getenv(ENV_APP_KEY));
        String authorization = firstNonBlank(System.getenv(ENV_AUTHORIZATION));
        if (appId == null || appKey == null || authorization == null) {
            throw new IllegalStateException(
                    "Set " + ENV_APP_ID + ", " + ENV_APP_KEY + ", and " + ENV_AUTHORIZATION
                            + " (see README). Do not commit credentials.");
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
