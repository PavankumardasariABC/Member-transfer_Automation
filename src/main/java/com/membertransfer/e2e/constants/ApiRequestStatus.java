package com.membertransfer.e2e.constants;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Mirrors {@code com.abcfinancial.constants.ApiRequestStatus} (dt2rcm_automation) for eAPI status payloads.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum ApiRequestStatus {
    SUCCESS("success");

    private String status;
}
