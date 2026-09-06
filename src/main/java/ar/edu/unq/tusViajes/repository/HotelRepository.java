package ar.edu.unq.tusViajes.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.unq.tusViajes.model.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

}
