package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.AgencyStatus;

public class AgencyBuilder {

    private String email = "travel@example.com";
    private String passwordHash = "$2a$10$hashedPasswordPlaceholder";
    private String businessName = "Travel Agency";
    private String taxId = "20-12345678-3";
    private AgencyStatus status = AgencyStatus.AUTHORIZED;

    public static AgencyBuilder anAgency() {
        return new AgencyBuilder();
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
        return new Agency(email, passwordHash, businessName, taxId, status);
    }
}
