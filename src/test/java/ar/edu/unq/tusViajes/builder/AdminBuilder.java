package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.Admin;
import org.springframework.test.util.ReflectionTestUtils;

public class AdminBuilder {

    private Long id = null;
    private String firstName = "John";
    private String lastName = "Doe";
    private String email = "admin@example.com";
    private String passwordHash = "$2a$10$hashedPasswordPlaceholder";

    public static AdminBuilder anAdmin() {
        return new AdminBuilder();
    }

    public AdminBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public AdminBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public AdminBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public AdminBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public AdminBuilder withPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public Admin build() {
        Admin admin = new Admin(firstName, lastName, email, passwordHash);
        if (id != null) {
            ReflectionTestUtils.setField(admin, "id", id);
        }
        return admin;
    }
}
