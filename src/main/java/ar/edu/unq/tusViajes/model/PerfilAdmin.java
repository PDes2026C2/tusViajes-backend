package ar.edu.unq.tusViajes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
 
@Entity
@Table(name = "perfil_administrador")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PerfilAdmin extends Usuario {
 
    public PerfilAdmin(String nombre, String apellido, String email, String passwordHash) {
        super(nombre, apellido, email, passwordHash);
    }
 
    public void actualizarDatos(String nombre, String apellido) {
        actualizarDatosBasicos(nombre, apellido);
    }
}
 