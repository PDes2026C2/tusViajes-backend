package ar.edu.unq.tusViajes.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class TravelPackageMetrics {

    private static final String SEARCHES = "tusviajes.package.searches";
    private static final String DESTINATION_SEARCHES = "tusviajes.package.destination.searches";
    private static final Pattern COUNTRY_CODE = Pattern.compile("[A-Za-z]{2}");

    private final Counter found;
    private final Counter empty;
    private final Counter otherCities;
    private final Map<String, Counter> countries;
    private final Map<Long, Counter> monitoredCities;

    public TravelPackageMetrics(MeterRegistry registry, TravelPackageMetricsProperties properties) {
        found = registry.counter(SEARCHES, "result", "found");
        empty = registry.counter(SEARCHES, "result", "empty");
        otherCities = registry.counter(DESTINATION_SEARCHES, "type", "city", "destination", "other");

        // Evita que la primera muestra de una serie ya incluya su primer incremento.
        countries = Arrays.stream(Locale.getISOCountries()).collect(Collectors.toUnmodifiableMap(
                Function.identity(),
                code -> registry.counter(DESTINATION_SEARCHES, "type", "country", "destination", code)));
        monitoredCities = properties.monitoredCityIds().stream().collect(Collectors.toUnmodifiableMap(
                Function.identity(),
                id -> registry.counter(DESTINATION_SEARCHES, "type", "city", "destination", id.toString())));
    }

    public void recordSearch(boolean hasResults, String destinationCountryIso, Long destinationCityId) {
        (hasResults ? found : empty).increment();
        recordCountry(destinationCountryIso);
        if (destinationCityId != null && destinationCityId > 0) {
            monitoredCities.getOrDefault(destinationCityId, otherCities).increment();
        }
    }

    private void recordCountry(String destinationCountryIso) {
        if (destinationCountryIso == null) {
            return;
        }
        String code = destinationCountryIso.trim();
        if (!COUNTRY_CODE.matcher(code).matches()) {
            return;
        }
        Counter counter = countries.get(code.toUpperCase(Locale.ROOT));
        if (counter != null) {
            counter.increment();
        }
    }
}
