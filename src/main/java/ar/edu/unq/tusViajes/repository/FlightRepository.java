package ar.edu.unq.tusViajes.repository;


import ar.edu.unq.tusViajes.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightRepository extends JpaRepository<Flight, Long> {

}
