package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.Admin;

public class AdminBuilder {

    private String nombre = "Admin";
    private String apellido = "Root";
    private String email = "admin@tusviajes.com";
    private String passwordHash = "$2a$10$hashedPasswordPlaceholder";

    public static AdminBuilder anAdmin() {
        return new AdminBuilder();
    }

    public AdminBuilder withNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public AdminBuilder withApellido(String apellido) {
        this.apellido = apellido;
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
        return new Admin(nombre, apellido, email, passwordHash);
    }
}
