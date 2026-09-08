package ar.edu.unq.tusViajes.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AdminTest {

    @Test
    void createAdmin_assignsDataCorrectly() {
        Admin admin = new Admin("Root", "Admin", "root@tusviajes.com", "hash123");

        assertThat(admin.getFirstName()).isEqualTo("Root");
        assertThat(admin.getLastName()).isEqualTo("Admin");
        assertThat(admin.getEmail()).isEqualTo("root@tusviajes.com");
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(admin.isActive()).isTrue();
    }

    @Test
    void updateData_modifiesFirstAndLastName() {
        Admin admin = new Admin("Root", "Admin", "root@tusviajes.com", "hash123");

        admin.updateData("Super", "User");

        assertThat(admin.getFirstName()).isEqualTo("Super");
        assertThat(admin.getLastName()).isEqualTo("User");
    }
}
