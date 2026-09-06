package ar.edu.unq.tusViajes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.unq.tusViajes.model.Agencia;

public interface AgenciaRepository extends JpaRepository<Agencia, Long> {
 
    boolean existsByCuit(String cuit);
}
