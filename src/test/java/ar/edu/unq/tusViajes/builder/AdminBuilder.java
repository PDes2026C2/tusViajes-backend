package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.Admin;

public class AdminBuilder {

    private String firstName = "Admin";
    private String lastName = "Root";
    private String email = "admin-builder@tusviajes.com";
    private String passwordHash = "$2a$10$hashedPasswordPlaceholder";

    public static AdminBuilder anAdmin() {
        return new AdminBuilder();
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
        return new Admin(firstName, lastName, email, passwordHash);
    }
}
