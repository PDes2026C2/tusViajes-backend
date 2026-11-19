package ar.edu.unq.tusViajes.metrics;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Validated
@ConfigurationProperties("tusviajes.metrics")
public record TravelPackageMetricsProperties(@Size(max = 50) Set<@Positive Long> monitoredCityIds) {

    public TravelPackageMetricsProperties {
        monitoredCityIds = monitoredCityIds == null ? Set.of() : Set.copyOf(monitoredCityIds);
    }
}
