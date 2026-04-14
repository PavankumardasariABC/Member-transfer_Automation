package com.membertransfer.e2e.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

/**
 * Writes one JSON file per shard under {@code build/e2e-agreement-results/} for CI artifacts and summaries.
 */
public final class E2eAgreementResultRecorder {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final String DEFAULT_RESULTS_DIR = "build/e2e-agreement-results";

    private E2eAgreementResultRecorder() {
    }

    public static Path resultsDirectory() {
        String override = firstNonBlank(System.getProperty("e2e.resultsDir"), System.getenv("E2E_RESULTS_DIR"));
        String dir = override != null && !override.isBlank() ? override.trim() : DEFAULT_RESULTS_DIR;
        return Path.of(dir);
    }

    public static ObjectNode baseRunFields(
            String environmentProfile,
            String eapiBaseUrl,
            String clubNumber,
            String paymentPlan,
            int shardIndex,
            int shardCount,
            int totalAgreements
    ) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("environmentProfile", nullToEmpty(environmentProfile));
        n.put("eapiBaseUrl", nullToEmpty(eapiBaseUrl));
        n.put("clubNumber", nullToEmpty(clubNumber));
        n.put("paymentPlan", nullToEmpty(paymentPlan));
        n.put("shardIndex", shardIndex);
        n.put("shardCount", shardCount);
        n.put("totalAgreements", totalAgreements);
        n.put("recordedAt", Instant.now().toString());
        n.put("githubRunId", nullToEmpty(System.getenv("GITHUB_RUN_ID")));
        n.put("githubRepository", nullToEmpty(System.getenv("GITHUB_REPOSITORY")));
        n.put("githubServerUrl", nullToEmpty(System.getenv("GITHUB_SERVER_URL")));
        n.put("githubWorkflow", nullToEmpty(System.getenv("GITHUB_WORKFLOW")));
        return n;
    }

    public static ObjectNode agreementRow(
            int agreementIndex0,
            int agreementOrdinal1,
            int totalAgreements,
            String clubNumber,
            String paymentPlan,
            String memberId,
            String agreementNumber,
            String barcode,
            String memberName
    ) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("status", "SUCCESS");
        n.put("agreementIndex", agreementIndex0);
        n.put("agreementOrdinal", agreementOrdinal1);
        n.put("totalAgreements", totalAgreements);
        n.put("clubNumber", clubNumber);
        n.put("paymentPlan", paymentPlan);
        n.put("memberId", memberId);
        n.put("agreementNumber", agreementNumber);
        n.put("barcode", barcode);
        n.put("memberName", memberName);
        n.put("createdAt", Instant.now().toString());
        return n;
    }

    public static ObjectNode failureRow(String message) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("status", "FAILED");
        n.put("message", nullToEmpty(message));
        n.put("createdAt", Instant.now().toString());
        return n;
    }

    /**
     * Writes {@code agreements-shard-{shardIndex}.json} as a JSON array: [ run meta (optional), ...rows ].
     * First element is always an object with type {@code run} when {@code runMeta} is non-null.
     */
    public static void writeShardFile(int shardIndex, ObjectNode runMeta, List<ObjectNode> agreementRows, Throwable failure) {
        try {
            Path dir = resultsDirectory();
            Files.createDirectories(dir);
            ArrayNode root = MAPPER.createArrayNode();
            if (runMeta != null) {
                ObjectNode wrap = MAPPER.createObjectNode();
                wrap.put("type", "run");
                wrap.set("data", runMeta);
                root.add(wrap);
            }
            for (ObjectNode row : agreementRows) {
                root.add(row);
            }
            if (failure != null) {
                ObjectNode wrap = MAPPER.createObjectNode();
                wrap.put("type", "failure");
                wrap.set("data", failureRow(failure.getMessage()));
                root.add(wrap);
            }
            Path out = dir.resolve("agreements-shard-" + shardIndex + ".json");
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(out.toFile(), root);
        } catch (Exception e) {
            System.err.println("Could not write E2E agreement results file: " + e.getMessage());
        }
    }

    public static String runSummaryMarkdownLink() {
        String server = nullToEmpty(System.getenv("GITHUB_SERVER_URL"));
        String repo = nullToEmpty(System.getenv("GITHUB_REPOSITORY"));
        String run = nullToEmpty(System.getenv("GITHUB_RUN_ID"));
        if (server.isEmpty() || repo.isEmpty() || run.isEmpty()) {
            return "";
        }
        return server + "/" + repo + "/actions/runs/" + run;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
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
