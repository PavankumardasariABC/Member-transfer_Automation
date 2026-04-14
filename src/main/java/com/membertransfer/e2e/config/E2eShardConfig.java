package com.membertransfer.e2e.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Sharded agreement runs: {@code E2E_TOTAL_AGREEMENTS} agreements are split across
 * {@code E2E_SHARD_COUNT} parallel runners (GitHub matrix jobs). This JVM handles
 * {@code E2E_SHARD_INDEX} and creates agreements at indices {@code index, index+shardCount, ...}.
 */
public final class E2eShardConfig {

    public static final String PROP_TOTAL = "e2e.totalAgreements";
    public static final String ENV_TOTAL = "E2E_TOTAL_AGREEMENTS";

    public static final String PROP_SHARD_INDEX = "e2e.shardIndex";
    public static final String ENV_SHARD_INDEX = "E2E_SHARD_INDEX";

    public static final String PROP_SHARD_COUNT = "e2e.shardCount";
    public static final String ENV_SHARD_COUNT = "E2E_SHARD_COUNT";

    private E2eShardConfig() {
    }

    public static int totalAgreements() {
        return parsePositiveInt(firstNonBlank(
                System.getProperty(PROP_TOTAL),
                System.getenv(ENV_TOTAL)
        ), 1, PROP_TOTAL + " / " + ENV_TOTAL);
    }

    public static int shardIndex() {
        return parseNonNegativeInt(firstNonBlank(
                System.getProperty(PROP_SHARD_INDEX),
                System.getenv(ENV_SHARD_INDEX)
        ), 0, PROP_SHARD_INDEX + " / " + ENV_SHARD_INDEX);
    }

    public static int shardCount() {
        int n = parsePositiveInt(firstNonBlank(
                System.getProperty(PROP_SHARD_COUNT),
                System.getenv(ENV_SHARD_COUNT)
        ), 1, PROP_SHARD_COUNT + " / " + ENV_SHARD_COUNT);
        return n;
    }

    /**
     * 0-based agreement indices this shard must create, in order.
     */
    public static List<Integer> agreementIndicesForThisShard() {
        int total = totalAgreements();
        int shards = shardCount();
        int index = shardIndex();
        if (index < 0 || index >= shards) {
            throw new IllegalStateException("Invalid shard index " + index + " for shardCount " + shards);
        }
        List<Integer> out = new ArrayList<>();
        for (int k = index; k < total; k += shards) {
            out.add(k);
        }
        return out;
    }

    public static void validateOrThrow() {
        int total = totalAgreements();
        int shards = shardCount();
        int index = shardIndex();
        if (total < 1 || total > 100) {
            throw new IllegalStateException("total agreements must be between 1 and 100, got " + total);
        }
        if (shards < 1 || shards > total) {
            throw new IllegalStateException("shard count must be >= 1 and <= total (" + total + "), got " + shards);
        }
        if (index < 0 || index >= shards) {
            throw new IllegalStateException("shard index must be in [0, " + (shards - 1) + "], got " + index);
        }
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return null;
    }

    private static int parsePositiveInt(String raw, int defaultValue, String label) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        int v = Integer.parseInt(raw.trim());
        if (v < 1) {
            throw new IllegalStateException("Expected positive integer for " + label + ", got " + raw);
        }
        return v;
    }

    private static int parseNonNegativeInt(String raw, int defaultValue, String label) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        int v = Integer.parseInt(raw.trim());
        if (v < 0) {
            throw new IllegalStateException("Expected non-negative integer for " + label + ", got " + raw);
        }
        return v;
    }
}
