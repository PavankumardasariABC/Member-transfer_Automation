package com.membertransfer.e2e.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Mirrors {@code com.abcfinancial.constants.PaymentSlot} (dt2rcm_automation).
 */
@Getter
@AllArgsConstructor
public enum PaymentSlot {
    CARD_ON_FILE("CardOnFile"),
    CLUB_BILLING("ClubBilling"),
    CLUB_ACCOUNT("ClubAccount");

    private final String eapiValue;
}
