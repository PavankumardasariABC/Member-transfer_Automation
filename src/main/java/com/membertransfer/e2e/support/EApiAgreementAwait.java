package com.membertransfer.e2e.support;

import com.membertransfer.e2e.config.EApiEnvironment;
import com.membertransfer.e2e.constants.QueueStatus;
import com.membertransfer.e2e.eapi.EApiAgreementClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;

/**
 * Mirrors dt2rcm {@code ApiAwaitUtils.waitForAgreementHasQueueStatus}: poll member info until agreement queue matches.
 */
public final class EApiAgreementAwait {

    private static final Logger log = LoggerFactory.getLogger(EApiAgreementAwait.class);

    private EApiAgreementAwait() {
    }

    /**
     * Default 40 minutes, 15-second poll — same as dt2rcm {@code ApiAwaitUtils} for agreement queue.
     */
    public static void waitForAgreementHasQueueStatus(
            QueueStatus expected,
            EApiAgreementClient client,
            String clubNumber,
            String memberId) {
        Duration timeout = EApiEnvironment.agreementQueueWaitTimeout();
        Duration poll = Duration.ofSeconds(15);
        log.info("Wait for Agreement queue status: {} (timeout={}, poll={})", expected.getApiValue(), timeout, poll);
        await().atMost(timeout.toMillis(), MILLISECONDS)
                .pollInterval(poll.toMillis(), MILLISECONDS)
                .conditionEvaluationListener(ev ->
                        log.info("[Await] agreement queue: elapsed={}ms remaining={}ms",
                                ev.getElapsedTimeInMS(),
                                ev.getRemainingTimeInMS()))
                .until(() -> expected.getApiValue().equals(client.getMemberInfo(clubNumber, memberId).getCurrentQueue()));
    }
}
