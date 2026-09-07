package ar.edu.unq.tusViajes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.unq.tusViajes.model.Comprador;

public interface CompradorRepository extends JpaRepository<Comprador, Long> {
}
