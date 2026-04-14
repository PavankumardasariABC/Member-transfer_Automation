package com.membertransfer.e2e.apps.pg;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Copied from dt2rcm_automation {@code com.abcfinancial.api.apps.pg.CardBrand} for payment-method UI assertions.
 */
@Getter
@AllArgsConstructor
public enum CardBrand {
    CC_AMEX("American Express", "AMEX", "3", "americanexpress"),
    CC_MASTERCARD("Master Card", "MASTERCARD", "2", "mastercard"),
    CC_VISA("Visa", "VISA", "0", "visa"),
    CC_DISCOVER("Discover", "DISCOVER", "1", "discover"),
    CC_ALL("All", "?", "?", ""),
    CC_NA("N/a", "?", "?", "");

    private final String uiValue;
    private final String apiValue;
    private final String numericType;
    private final String eapiValue;

    public static CardBrand getCardBrandByUiValue(String uiValue) {
        return Arrays.stream(values())
                .filter(brand -> brand.uiValue.equals(uiValue))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    public static CardBrand getCardBrandByDtPaymentMethodType(String paymentMethodTypeByDtPmType) {
        return Arrays.stream(values())
                .filter(brand -> brand.apiValue.startsWith(paymentMethodTypeByDtPmType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such payment method type: " + paymentMethodTypeByDtPmType));
    }

    public static CardBrand getCardBrandByEapiValue(String eapiValue) {
        return Arrays.stream(values())
                .filter(brand -> brand.eapiValue.equals(eapiValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such payment method type: " + eapiValue));
    }
}
