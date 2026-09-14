package ar.edu.unq.tusViajes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "flights")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Flight {

    @Id
    private Long id;

    @Column(nullable = false)
    private String airline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_city_id", nullable = false)
    private City originCity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_city_id", nullable = false)
    private City destinationCity;

    @Column(name = "departure_date", nullable = false)
    private LocalDateTime departureDate;

    @Column(name = "arrival_date", nullable = false)
    private LocalDateTime arrivalDate;
}
