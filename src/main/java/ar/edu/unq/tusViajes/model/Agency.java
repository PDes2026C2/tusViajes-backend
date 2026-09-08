package ar.edu.unq.tusViajes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "agencies")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Agency extends User {

    @Column(name = "business_name", nullable = false, length = 150)
    private String businessName;

    @Column(name = "tax_id", nullable = false, length = 13, unique = true)
    private String taxId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AgencyStatus status = AgencyStatus.PENDING;

    public Agency(String email, String passwordHash, String businessName, String taxId) {
        super(email, passwordHash);
        this.businessName = businessName;
        this.taxId = taxId;
        this.status = AgencyStatus.PENDING;
    }

    public Agency(String email, String passwordHash, String businessName, String taxId, AgencyStatus status) {
        super(email, passwordHash);
        this.businessName = businessName;
        this.taxId = taxId;
        this.status = status;
    }

    public void updateBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public void authorize() {
        this.status = AgencyStatus.AUTHORIZED;
    }

    public void reject() {
        this.status = AgencyStatus.REJECTED;
    }

    public boolean isAuthorized() {
        return AgencyStatus.AUTHORIZED.equals(this.status);
    }

    @Override
    public Role getRole() {
        return Role.AGENCY;
    }

    @Override
    public boolean isActive() {
        return isAuthorized();
    }

    @Override
    public String getVisualIdentifier() {
        return businessName;
    }
}
