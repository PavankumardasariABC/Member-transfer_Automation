package com.membertransfer.e2e.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Mirrors {@code com.abcfinancial.constants.PaymentMethodsType} (dt2rcm_automation) subset used by agreement E2E.
 */
@Getter
@AllArgsConstructor
public enum PaymentMethodsType {
    /** eAPI member wallet returns display string {@code "Credit Card"} for this type (matches dt2rcm {@code CREDIT_CARD.getEapi()}). */
    CREDIT_CARD("Credit Card", "CREDIT_CARD", "Credit Card");

    private final String ui;
    private final String api;
    private final String eapi;
}
