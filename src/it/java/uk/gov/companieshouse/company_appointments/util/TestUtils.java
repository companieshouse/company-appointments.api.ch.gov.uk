package uk.gov.companieshouse.company_appointments.util;

import java.util.Random;

public class TestUtils {

    public static final String[] IDENTITY_TYPES = {
            "uk-limited-company",
            "eea",
            "non-eea",
            "other-corporate-body-or-firm",
            "registered-overseas-entity-corporate-managing-officer" };

    public static final String[] COMPANY_STATUSES = {
            "active",
            "liquidation",
            "receivership",
            "voluntary-arrangement",
            "insolvency-proceedings",
            "administration",
            "open",
            "registered",
            "removed",
            "dissolved",
            "converted-closed",
            "closed" };

    public static final String[] CORPORATE_APPOINTMENT_DOC_PATHS = {
            "/appointmentdocuments/corp_active_delta_appointment_document_template.json",
            "/appointmentdocuments/corp_pre_1992_delta_appointment_document_template.json",
            "/appointmentdocuments/corp_resigned_delta_appointment_document_template.json" };

    public static final String[] NATURAL_APPOINTMENT_DOC_PATHS = {
            "/appointmentdocuments/nat_active_delta_appointment_document_template.json",
            "/appointmentdocuments/nat_pre_1992_delta_appointment_document_template.json",
            "/appointmentdocuments/nat_resigned_delta_appointment_document_template.json" };

    public static final String[] OFFICER_ROLES = {
            "cic-manager",
            "corporate-director",
            "corporate-llp-designated-member",
            "corporate-llp-member",
            "corporate-managing-officer",
            "corporate-member-of-a-management-organ",
            "corporate-member-of-a-supervisory-organ",
            "corporate-member-of-an-administrative-organ",
            "corporate-nominee-director",
            "corporate-nominee-secretary",
            "corporate-secretary",
            "director",
            "judicial-factor",
            "llp-designated-member",
            "llp-member",
            "managing-officer",
            "member-of-a-management-organ",
            "member-of-a-supervisory-organ",
            "member-of-an-administrative-organ",
            "nominee-director",
            "nominee-secretary",
            "receiver-and-manager",
            "secretary" };

    static public String generateRandomInternalId() {
        final int low = 10000;
        final int high = 99999;

        final int result = generateRandomNumberWithinBounds(high, low);

        return String.format("12345%d", result);
    }

    static public String generateRandomEightCharCompanyNumber() {
        final int low = 100000;
        final int high = 999999;

        final int result = generateRandomNumberWithinBounds(high, low);

        return String.format("CN%d", result);
    }

    static public int generateRandomNumberWithinBounds(final int upperBound, final int lowerBound) {
        return new Random().nextInt(upperBound - lowerBound) + lowerBound;
    }
}
