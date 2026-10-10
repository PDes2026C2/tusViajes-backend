package ar.edu.unq.tusViajes.specification;

import ar.edu.unq.tusViajes.controller.dto.request.TravelPackageFilterDTO;
import ar.edu.unq.tusViajes.model.*;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public final class TravelPackageSpecifications {

    private TravelPackageSpecifications() {
    }

    public static Specification<TravelPackage> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<TravelPackage> originCountryIs(String isoCode) {
        if (!StringUtils.hasText(isoCode)) {
            return null;
        }
        return (root, query, cb) -> {
            Join<TravelPackage, Flight> flight = getOrCreateJoin(root, "departureFlight", JoinType.INNER);
            Join<Flight, City> city = getOrCreateJoin(flight, "originCity", JoinType.INNER);
            Join<City, Country> country = getOrCreateJoin(city, "country", JoinType.INNER);
            return cb.equal(cb.upper(country.get("isoCode")), isoCode.trim().toUpperCase());
        };
    }

    public static Specification<TravelPackage> originCityIdIs(Long cityId) {
        if (cityId == null || cityId <= 0) {
            return null;
        }
        return (root, query, cb) -> {
            Join<TravelPackage, Flight> flight = getOrCreateJoin(root, "departureFlight", JoinType.INNER);
            return cb.equal(flight.get("originCity").get("id"), cityId);
        };
    }

    public static Specification<TravelPackage> destinationCountryIs(String isoCode) {
        if (!StringUtils.hasText(isoCode)) {
            return null;
        }
        return (root, query, cb) -> {
            Join<TravelPackage, Hotel> hotel = getOrCreateJoin(root, "hotel", JoinType.INNER);
            Join<Hotel, City> city = getOrCreateJoin(hotel, "city", JoinType.INNER);
            Join<City, Country> country = getOrCreateJoin(city, "country", JoinType.INNER);
            return cb.equal(cb.upper(country.get("isoCode")), isoCode.trim().toUpperCase());
        };
    }

    public static Specification<TravelPackage> destinationCityIdIs(Long cityId) {
        if (cityId == null || cityId <= 0) {
            return null;
        }
        return (root, query, cb) -> {
            Join<TravelPackage, Hotel> hotel = getOrCreateJoin(root, "hotel", JoinType.INNER);
            return cb.equal(hotel.get("city").get("id"), cityId);
        };
    }

    public static Specification<TravelPackage> departureAfter(LocalDateTime from) {
        if (from == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startDate"), from);
    }

    public static Specification<TravelPackage> departureBefore(LocalDateTime to) {
        if (to == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startDate"), to);
    }

    public static Specification<TravelPackage> arrivalAfter(LocalDateTime from) {
        if (from == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("endDate"), from);
    }

    public static Specification<TravelPackage> arrivalBefore(LocalDateTime to) {
        if (to == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("endDate"), to);
    }

    public static Specification<TravelPackage> fromFilter(TravelPackageFilterDTO filter) {
        return (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));

            if (filter == null) {
                return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
            }

            if (StringUtils.hasText(filter.originCountryIso())) {
                Join<TravelPackage, Flight> flight = getOrCreateJoin(root, "departureFlight", JoinType.INNER);
                Join<Flight, City> city = getOrCreateJoin(flight, "originCity", JoinType.INNER);
                Join<City, Country> country = getOrCreateJoin(city, "country", JoinType.INNER);
                predicates.add(cb.equal(cb.upper(country.get("isoCode")), filter.originCountryIso().trim().toUpperCase()));
            }

            if (filter.originCityId() != null && filter.originCityId() > 0) {
                Join<TravelPackage, Flight> flight = getOrCreateJoin(root, "departureFlight", JoinType.INNER);
                predicates.add(cb.equal(flight.get("originCity").get("id"), filter.originCityId()));
            }

            if (StringUtils.hasText(filter.destinationCountryIso())) {
                Join<TravelPackage, Hotel> hotel = getOrCreateJoin(root, "hotel", JoinType.INNER);
                Join<Hotel, City> city = getOrCreateJoin(hotel, "city", JoinType.INNER);
                Join<City, Country> country = getOrCreateJoin(city, "country", JoinType.INNER);
                predicates.add(cb.equal(cb.upper(country.get("isoCode")), filter.destinationCountryIso().trim().toUpperCase()));
            }

            if (filter.destinationCityId() != null && filter.destinationCityId() > 0) {
                Join<TravelPackage, Hotel> hotel = getOrCreateJoin(root, "hotel", JoinType.INNER);
                predicates.add(cb.equal(hotel.get("city").get("id"), filter.destinationCityId()));
            }

            if (filter.departureFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), filter.departureFrom()));
            }
            if (filter.departureTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), filter.departureTo()));
            }

            if (filter.arrivalFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("endDate"), filter.arrivalFrom()));
            }
            if (filter.arrivalTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), filter.arrivalTo()));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    @SuppressWarnings("unchecked")
    private static <X, Y> Join<X, Y> getOrCreateJoin(From<?, X> from, String attributeName, JoinType joinType) {
        for (Join<X, ?> join : from.getJoins()) {
            if (join.getAttribute().getName().equals(attributeName)) {
                return (Join<X, Y>) join;
            }
        }
        return from.join(attributeName, joinType);
    }
}
