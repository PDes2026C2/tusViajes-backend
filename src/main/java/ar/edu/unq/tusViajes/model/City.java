package ar.edu.unq.tusViajes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="cities",
        schema = "world",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "countryISO"}),
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class City {
    @Id
    @GeneratedValue
    public Long id;

    @Column(name = "name", nullable = false)
    public String name;

    @ManyToOne
    @JoinColumn(name = "countryISO")
    public Country country;

    public City(String name, Country country) {
        this.name = name;
        this.country = country;
    }

    public boolean equals(City city) {
        return this.name.equals(city.name) && this.country.equals(city.country);
    }
}