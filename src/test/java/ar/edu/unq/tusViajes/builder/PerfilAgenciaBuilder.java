package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.model.PerfilAgencia;

public class PerfilAgenciaBuilder {

    private String nombre = "Agustin";
    private String apellido = "Gomez";
    private String email = "agustin@agencia.com";
    private String passwordHash = "$2a$10$hashedPasswordPlaceholder";
    private Agencia agencia = AgenciaBuilder.anAgencia().build();

    public static PerfilAgenciaBuilder aPerfilAgencia() {
        return new PerfilAgenciaBuilder();
    }

    public PerfilAgenciaBuilder withNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public PerfilAgenciaBuilder withApellido(String apellido) {
        this.apellido = apellido;
        return this;
    }

    public PerfilAgenciaBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public PerfilAgenciaBuilder withPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public PerfilAgenciaBuilder withAgencia(Agencia agencia) {
        this.agencia = agencia;
        return this;
    }

    public PerfilAgencia build() {
        return new PerfilAgencia(nombre, apellido, email, passwordHash, agencia);
    }
}

