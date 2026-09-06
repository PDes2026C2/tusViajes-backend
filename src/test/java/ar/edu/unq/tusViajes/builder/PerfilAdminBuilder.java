package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.PerfilAdmin;

public class PerfilAdminBuilder {

    private String nombre = "Admin";
    private String apellido = "General";
    private String email = "admin@tusviajes.com";
    private String passwordHash = "$2a$10$hashedPasswordPlaceholder";

    public static PerfilAdminBuilder aPerfilAdmin() {
        return new PerfilAdminBuilder();
    }

    public PerfilAdminBuilder withNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public PerfilAdminBuilder withApellido(String apellido) {
        this.apellido = apellido;
        return this;
    }

    public PerfilAdminBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public PerfilAdminBuilder withPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public PerfilAdmin build() {
        return new PerfilAdmin(nombre, apellido, email, passwordHash);
    }
}

