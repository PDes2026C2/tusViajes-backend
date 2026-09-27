package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.AgencyStatus;
import org.springframework.test.util.ReflectionTestUtils;

public class AgencyBuilder {

    private Long id = null;
    private String email = "travel@example.com";
    private String passwordHash = "$2a$10$hashedPasswordPlaceholder";
    private String businessName = "Travel Agency";
    private String taxId = "20-12345678-3";
    private AgencyStatus status = AgencyStatus.AUTHORIZED;

    public static AgencyBuilder anAgency() {
        return new AgencyBuilder();
    }

    public AgencyBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public AgencyBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public AgencyBuilder withPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public AgencyBuilder withBusinessName(String businessName) {
        this.businessName = businessName;
        return this;
    }

    public AgencyBuilder withTaxId(String taxId) {
        this.taxId = taxId;
        return this;
    }

    public AgencyBuilder withStatus(AgencyStatus status) {
        this.status = status;
        return this;
    }

    public Agency build() {
        Agency agency = new Agency(email, passwordHash, businessName, taxId, status);
        if (id != null) {
            ReflectionTestUtils.setField(agency, "id", id);
        }
        return agency;
    }
}
