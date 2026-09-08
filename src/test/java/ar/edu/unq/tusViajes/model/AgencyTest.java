package ar.edu.unq.tusViajes.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AgencyTest {

    @Test
    void constructor_assignsAllFields() {
        Agency agency = new Agency("travel@test.com", "hash123", "Huryn", "20-44576859-8");

        assertThat(agency.getEmail()).isEqualTo("travel@test.com");
        assertThat(agency.getPasswordHash()).isEqualTo("hash123");
        assertThat(agency.getBusinessName()).isEqualTo("Huryn");
        assertThat(agency.getTaxId()).isEqualTo("20-44576859-8");
        assertThat(agency.getStatus()).isEqualTo(AgencyStatus.PENDING);
        assertThat(agency.getRole()).isEqualTo(Role.AGENCY);
        assertThat(agency.isAuthorized()).isFalse();
        assertThat(agency.isActive()).isFalse();
    }

    @Test
    void updateBusinessName_onlyChangesBusinessName() {
        Agency agency = new Agency("travel@test.com", "hash123", "Huryn", "20-44576859-8");

        agency.updateBusinessName("Metal");

        assertThat(agency.getBusinessName()).isEqualTo("Metal");
    }

    @Test
    void authorize_changesStatusToAuthorizedAndActivatesUser() {
        Agency agency = new Agency("travel@test.com", "hash123", "Travel Express", "30-12345678-9");
        assertThat(agency.isActive()).isFalse();

        agency.authorize();

        assertThat(agency.getStatus()).isEqualTo(AgencyStatus.AUTHORIZED);
        assertThat(agency.isAuthorized()).isTrue();
        assertThat(agency.isActive()).isTrue();
    }

    @Test
    void reject_changesStatusToRejectedAndDeactivatesUser() {
        Agency agency = new Agency("travel@test.com", "hash123", "Travel Express", "30-12345678-9");

        agency.reject();

        assertThat(agency.getStatus()).isEqualTo(AgencyStatus.REJECTED);
        assertThat(agency.isAuthorized()).isFalse();
        assertThat(agency.isActive()).isFalse();
    }
}
