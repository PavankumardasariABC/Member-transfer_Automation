package com.membertransfer.e2e.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Mirrors {@code com.abcfinancial.constants.QueueStatus} (dt2rcm_automation).
 */
@Getter
@AllArgsConstructor
public enum QueueStatus {
    POSTED("Posted"),
    APPROVAL("Approval"),
    POS("POS");

    private final String apiValue;
}
