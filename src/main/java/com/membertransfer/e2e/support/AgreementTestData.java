package com.membertransfer.e2e.support;

import com.membertransfer.e2e.model.agreement.CreateAgreementRequest;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Synthetic member + draft card data suitable for non-production eAPI runs.
 */
public final class AgreementTestData {

    private static final DateTimeFormatter BIRTHDAY = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss XXX");

    private AgreementTestData() {
    }

    public static CreateAgreementRequest.AgreementContactInfo contactInfo() {
        String suffix = randomAlpha(5);
        return CreateAgreementRequest.AgreementContactInfo.builder()
                .firstName("James" + suffix)
                .lastName("Sullivan" + suffix)
                .email("e2e.agreement." + UUID.randomUUID() + "@example.com")
                .gender("M")
                .homePhone(phone())
                .cellPhone(phone())
                .workPhone(phone())
                .birthday(LocalDate.now(ZoneOffset.UTC).minusYears(22).atStartOfDay(ZoneOffset.UTC).format(BIRTHDAY))
                .agreementAddressInfo(address())
                .emergencyContact(CreateAgreementRequest.AgreementContactInfo.EmergencyContact.builder()
                        .ecFirstName("Emergency" + suffix)
                        .ecLastName("Contact" + suffix)
                        .ecPhone(phone())
                        .ecPhoneExtension("1")
                        .build())
                .build();
    }

    public static CreateAgreementRequest.DraftBillingInfo draftVisaBilling(String firstName, String lastName) {
        return CreateAgreementRequest.DraftBillingInfo.builder()
                .draftCreditCard(CreateAgreementRequest.DraftBillingInfo.DraftCreditCard.builder()
                        .creditCardFirstName(firstName)
                        .creditCardLastName(lastName)
                        .creditCardType("visa")
                        .creditCardAccountNumber(randomVisaPan())
                        .creditCardExpMonth(String.format("%02d", ThreadLocalRandom.current().nextInt(1, 13)))
                        .creditCardExpYear(String.valueOf(LocalDate.now().getYear() + 3))
                        .build())
                .build();
    }

    private static CreateAgreementRequest.AgreementContactInfo.AgreementAddressInfo address() {
        String uniq = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return CreateAgreementRequest.AgreementContactInfo.AgreementAddressInfo.builder()
                .addressLine1("100 Automation Way " + uniq)
                .addressLine2("Suite E2E")
                .city("Little Rock")
                .state("AR")
                .country("US")
                .zipCode("72201")
                .build();
    }

    private static String phone() {
        return "501555" + String.format("%04d", ThreadLocalRandom.current().nextInt(0, 10000));
    }

    private static String randomAlpha(int len) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(len);
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Random 16-digit Visa-style PAN with valid Luhn check digit.
     */
    public static String randomVisaPan() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int[] digits = new int[16];
        digits[0] = 4;
        for (int i = 1; i < 15; i++) {
            digits[i] = r.nextInt(0, 10);
        }
        for (int check = 0; check < 10; check++) {
            digits[15] = check;
            if (passesLuhn(digits)) {
                StringBuilder sb = new StringBuilder(16);
                for (int d : digits) {
                    sb.append(d);
                }
                return sb.toString();
            }
        }
        throw new IllegalStateException("Could not compute Luhn check digit");
    }

    private static boolean passesLuhn(int[] digits) {
        int sum = 0;
        for (int i = digits.length - 1; i >= 0; i--) {
            int n = digits[i];
            if ((digits.length - 1 - i) % 2 == 1) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
        }
        return sum % 10 == 0;
    }
}
