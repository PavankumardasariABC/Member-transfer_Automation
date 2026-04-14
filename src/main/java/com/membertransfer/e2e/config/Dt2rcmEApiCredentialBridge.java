package com.membertransfer.e2e.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * When {@code EAPI_APP_ID} / {@code EAPI_APP_KEY} / {@code EAPI_AUTHORIZATION} are unset, optionally
 * read the same literals from a local <strong>dt2rcm_automation</strong> checkout's {@code EApiHelper.java}
 * ({@code APP_ID_WITH_AUTH}, {@code APP_KEY_WITH_AUTH}, {@code BASIC}). This matches how {@code :obc}
 * tests run without exporting secrets, while keeping credentials out of this repository.
 */
public final class Dt2rcmEApiCredentialBridge {

    public static final String PROP_DT2RCM_ROOT = "e2e.dt2rcm.root";
    public static final String ENV_DT2RCM_ROOT = "DT2RCM_AUTOMATION_ROOT";

    private static final Pattern APP_ID = Pattern.compile("APP_ID_WITH_AUTH\\s*=\\s*\"([^\"]*)\"");
    private static final Pattern APP_KEY = Pattern.compile("APP_KEY_WITH_AUTH\\s*=\\s*\"([^\"]*)\"");
    private static final Pattern BASIC = Pattern.compile("String\\s+BASIC\\s*=\\s*\"([^\"]*)\"");

    private Dt2rcmEApiCredentialBridge() {
    }

    /**
     * @return {@code [appId, appKey, authorization]} if a helper file was found and parsed; otherwise {@code null}
     */
    public static String[] tryLoadAuthTriplet() {
        Path helper = resolveEApiHelperPath();
        if (helper == null || !Files.isRegularFile(helper)) {
            return null;
        }
        String src;
        try {
            src = Files.readString(helper, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
        String appId = firstGroup(APP_ID, src);
        String appKey = firstGroup(APP_KEY, src);
        String basic = firstGroup(BASIC, src);
        if (appId == null || appKey == null || basic == null) {
            return null;
        }
        return new String[]{appId, appKey, basic};
    }

    private static String firstGroup(Pattern p, String src) {
        Matcher m = p.matcher(src);
        return m.find() ? m.group(1) : null;
    }

    static Path resolveEApiHelperPath() {
        String override = firstNonBlank(System.getProperty(PROP_DT2RCM_ROOT), System.getenv(ENV_DT2RCM_ROOT));
        if (override != null && !override.isBlank()) {
            Path root = Path.of(override.trim()).toAbsolutePath().normalize();
            Path p = root.resolve(relativeHelperFromRepoRoot());
            if (Files.isRegularFile(p)) {
                return p;
            }
        }
        Path cwd = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        List<Path> candidates = new ArrayList<>();
        Path parent = cwd.getParent();
        if (parent != null) {
            candidates.add(parent.resolve("dt2rcm_automation-1").resolve(relativeHelperFromRepoRoot()));
            candidates.add(parent.resolve("dt2rcm_automation").resolve(relativeHelperFromRepoRoot()));
        }
        candidates.add(cwd.resolve("..").resolve("dt2rcm_automation-1").resolve(relativeHelperFromRepoRoot()).normalize());
        candidates.add(cwd.resolve("..").resolve("dt2rcm_automation").resolve(relativeHelperFromRepoRoot()).normalize());
        for (Path p : candidates) {
            if (Files.isRegularFile(p)) {
                return p;
            }
        }
        return null;
    }

    private static Path relativeHelperFromRepoRoot() {
        return Path.of("api", "src", "main", "java", "com", "abcfinancial", "api", "apps", "eapi", "EApiHelper.java");
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
}
