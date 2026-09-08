package ar.edu.unq.tusViajes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.unq.tusViajes.model.Buyer;

public interface BuyerRepository extends JpaRepository<Buyer, Long> {
}
