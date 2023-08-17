package uk.gov.companieshouse.company_appointments.logging;

import java.util.Map;
import uk.gov.companieshouse.logging.util.DataMap;
import uk.gov.companieshouse.logging.util.DataMap.Builder;

public class DataMapHolder {

    private static final ThreadLocal<DataMap.Builder> DATAMAP_BUILDER = ThreadLocal.withInitial(
            () -> new Builder().requestId("uninitialised"));

    private DataMapHolder() {
    }

    public static void initialise(String requestId) {
        DATAMAP_BUILDER.get().requestId(requestId);
    }

    public static void clear() {
        DATAMAP_BUILDER.remove();
    }

    public static DataMap.Builder get() {
        return DATAMAP_BUILDER.get();
    }

    /**
     * Used to populate the log context map in structured logging. e.g.
     * <code>
     * logger.error("Something happened", DataMapHolder.getLogMap());
     * </code>
     *
     * @return Structured logging DataMap.Builder
     */
    public static Map<String, Object> getLogMap() {
        return DATAMAP_BUILDER.get()
                .build()
                .getLogMap();
    }

    public static String getRequestId() {
        return (String) getLogMap().get("request_id");
    }
}