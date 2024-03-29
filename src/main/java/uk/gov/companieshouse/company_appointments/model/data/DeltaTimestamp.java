package uk.gov.companieshouse.company_appointments.model.data;

import java.time.Instant;
import java.util.Objects;

public class DeltaTimestamp {

    private Instant at;

    public DeltaTimestamp(Instant at) {
        this.at = at;
    }

    public Instant getAt() {
        return at;
    }

    public void setAt(Instant at) {
        this.at = at;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeltaTimestamp that = (DeltaTimestamp) o;
        return Objects.equals(getAt(), that.getAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAt());
    }
}
