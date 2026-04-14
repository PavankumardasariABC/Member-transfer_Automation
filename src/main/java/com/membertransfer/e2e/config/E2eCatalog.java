package com.membertransfer.e2e.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Loads {@code e2e/environments.json} and {@code e2e/clubs.json} from the classpath, or from an
 * optional filesystem override for clubs (see {@link #clubsOverridePath()}).
 */
public final class E2eCatalog {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ENVIRONMENTS_RESOURCE = "e2e/environments.json";
    private static final String CLUBS_RESOURCE = "e2e/clubs.json";

    public static final String PROP_ENV_PROFILE = "e2e.envProfile";
    public static final String ENV_ENV_PROFILE = "E2E_ENV_PROFILE";
    public static final String PROP_CLUBS_JSON_PATH = "e2e.clubsJsonPath";
    public static final String ENV_CLUBS_JSON_PATH = "E2E_CLUBS_JSON_PATH";

    private static volatile EnvironmentsDocument environmentsCache;
    private static volatile ClubsDocument clubsCache;
    private static volatile String clubsCacheKey;

    private E2eCatalog() {
    }

    public static EnvironmentsDocument environments() {
        if (environmentsCache == null) {
            synchronized (E2eCatalog.class) {
                if (environmentsCache == null) {
                    environmentsCache = readResource(ENVIRONMENTS_RESOURCE, EnvironmentsDocument.class);
                }
            }
        }
        return environmentsCache;
    }

    public static ClubsDocument clubs() {
        String override = clubsOverridePath();
        String key = (override != null && !override.isBlank())
                ? ("file:" + override)
                : ("classpath:" + CLUBS_RESOURCE);
        if (clubsCache != null && key.equals(clubsCacheKey)) {
            return clubsCache;
        }
        synchronized (E2eCatalog.class) {
            if (clubsCache != null && key.equals(clubsCacheKey)) {
                return clubsCache;
            }
            try {
                if (override != null && !override.isBlank()) {
                    Path p = Path.of(override);
                    if (!Files.isRegularFile(p)) {
                        throw new IllegalStateException("e2e clubs JSON path is not a file: " + p.toAbsolutePath());
                    }
                    clubsCache = MAPPER.readValue(p.toFile(), ClubsDocument.class);
                } else {
                    clubsCache = readResource(CLUBS_RESOURCE, ClubsDocument.class);
                }
                clubsCacheKey = key;
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read clubs catalog from " + key, e);
            }
            return clubsCache;
        }
    }

    private static String clubsOverridePath() {
        String p = firstNonBlank(System.getProperty(PROP_CLUBS_JSON_PATH), System.getenv(ENV_CLUBS_JSON_PATH));
        return p;
    }

    public static Optional<EnvironmentProfile> profile(String profileId) {
        if (profileId == null || profileId.isBlank()) {
            return Optional.empty();
        }
        EnvironmentProfile p = environments().getProfiles().get(profileId.trim());
        return Optional.ofNullable(p);
    }

    public static String profileIdsForMessage() {
        return environments().getProfiles().keySet().stream().sorted().collect(Collectors.joining(", "));
    }

    public static Optional<ClubEntry> findClub(String clubNumber) {
        if (clubNumber == null || clubNumber.isBlank()) {
            return Optional.empty();
        }
        String normalized = normalizeClubKey(clubNumber);
        for (ClubEntry c : clubs().getClubs()) {
            if (c.getClubNumber() != null && normalizeClubKey(c.getClubNumber()).equals(normalized)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    /**
     * Normalizes club keys so "6060" and "06060" match when the catalog uses 5-digit numbers.
     */
    public static String normalizeClubKey(String raw) {
        String digits = raw.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return raw.trim();
        }
        if (digits.length() < 5) {
            return String.format(Locale.ROOT, "%05d", Integer.parseInt(digits));
        }
        return digits;
    }

    public static void printClubContext(String clubNumber) {
        findClub(clubNumber).ifPresentOrElse(
                c -> {
                    System.out.println("--- Club catalog entry ---");
                    System.out.println("clubNumber=" + c.getClubNumber()
                            + " organizationName=" + c.getOrganizationName()
                            + " locationName=" + c.getLocationName());
                    if (c.getOrganizationId() != null) {
                        System.out.println("organizationId=" + c.getOrganizationId());
                    }
                    if (c.getLocationId() != null) {
                        System.out.println("locationId=" + c.getLocationId());
                    }
                    if (!c.getLinks().isEmpty()) {
                        System.out.println("links:");
                        c.getLinks().forEach((k, v) -> System.out.println("  " + k + ": " + v));
                    }
                },
                () -> System.out.println("--- No club catalog entry for " + clubNumber
                        + " (add it in src/main/resources/e2e/clubs.json) ---")
        );
    }

    public static void printEnvironmentContext(String profileId, EnvironmentProfile profile) {
        System.out.println("--- Environment profile ---");
        System.out.println("profileId=" + profileId);
        if (profile.getDescription() != null) {
            System.out.println("description=" + profile.getDescription());
        }
        System.out.println("eapiBaseUrl=" + profile.getEapiBaseUrl());
        if (profile.getRcmBillingUrl() != null) {
            System.out.println("rcmBillingUrl=" + profile.getRcmBillingUrl());
        }
        if (profile.getCommerceUiUrl() != null) {
            System.out.println("commerceUiUrl=" + profile.getCommerceUiUrl());
        }
    }

    private static <T> T readResource(String classpathLocation, Class<T> type) {
        try (InputStream in = E2eCatalog.class.getClassLoader().getResourceAsStream(classpathLocation)) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath resource: " + classpathLocation);
            }
            return MAPPER.readValue(in, type);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse " + classpathLocation, e);
        }
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }
}
