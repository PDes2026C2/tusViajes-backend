package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.PerfilComprador;

public class PerfilCompradorBuilder {

    private String nombre = "Juan";
    private String apellido = "Perez";
    private String email = "juan.perez@example.com";
    private String passwordHash = "$2a$10$hashedPasswordPlaceholder";
    private String telefono = "11-4567-8901";
    private String dni = "38123456";

    public static PerfilCompradorBuilder aPerfilComprador() {
        return new PerfilCompradorBuilder();
    }

    public PerfilCompradorBuilder withNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public PerfilCompradorBuilder withApellido(String apellido) {
        this.apellido = apellido;
        return this;
    }

    public PerfilCompradorBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public PerfilCompradorBuilder withPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public PerfilCompradorBuilder withTelefono(String telefono) {
        this.telefono = telefono;
        return this;
    }

    public PerfilCompradorBuilder withDni(String dni) {
        this.dni = dni;
        return this;
    }

    public PerfilComprador build() {
        return new PerfilComprador(nombre, apellido, email, passwordHash, telefono, dni);
    }
}

