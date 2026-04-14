package com.membertransfer.e2e.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Mirrors {@code com.abcfinancial.constants.MemberStatus} (dt2rcm_automation) — fields used by agreement E2E only.
 */
@Getter
@AllArgsConstructor
public enum MemberStatus {
    ACTIVE("Active", "Active");

    private final String uiValue;
    private final String apiValue;
}
