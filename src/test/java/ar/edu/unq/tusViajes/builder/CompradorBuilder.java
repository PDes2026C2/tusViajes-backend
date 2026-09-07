package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.Comprador;

public class CompradorBuilder {

    private String nombre = "Lucas";
    private String apellido = "Gomez";
    private String email = "lucas@example.com";
    private String passwordHash = "$2a$10$hashedPasswordPlaceholder";
    private String telefono = "11223344";
    private String dni = "38123456";

    public static CompradorBuilder aComprador() {
        return new CompradorBuilder();
    }

    public CompradorBuilder withNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public CompradorBuilder withApellido(String apellido) {
        this.apellido = apellido;
        return this;
    }

    public CompradorBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public CompradorBuilder withPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public CompradorBuilder withTelefono(String telefono) {
        this.telefono = telefono;
        return this;
    }

    public CompradorBuilder withDni(String dni) {
        this.dni = dni;
        return this;
    }

    public Comprador build() {
        return new Comprador(nombre, apellido, email, passwordHash, telefono, dni);
    }
}
