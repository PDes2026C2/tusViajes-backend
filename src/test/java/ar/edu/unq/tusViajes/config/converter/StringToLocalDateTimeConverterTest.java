package ar.edu.unq.tusViajes.config.converter;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class StringToLocalDateTimeConverterTest {

    private final StringToLocalDateTimeConverter converter = new StringToLocalDateTimeConverter();

    @Test
    void convert_returnsNull_whenSourceIsNullOrBlank() {
        assertThat(converter.convert(null)).isNull();
        assertThat(converter.convert("")).isNull();
        assertThat(converter.convert("   ")).isNull();
    }

    @Test
    void convert_parsesStandardIsoLocalDateTime() {
        LocalDateTime result = converter.convert("2026-10-15T10:30:00");
        assertThat(result).isEqualTo(LocalDateTime.of(2026, 10, 15, 10, 30, 0));
    }

    @Test
    void convert_parsesIsoStringWithUtcZoneOffset() {
        LocalDateTime result = converter.convert("2026-10-15T10:30:00.000Z");
        assertThat(result).isEqualTo(LocalDateTime.of(2026, 10, 15, 10, 30, 0));
    }

    @Test
    void convert_parsesDateOnlyString() {
        LocalDateTime result = converter.convert("2026-10-15");
        assertThat(result).isEqualTo(LocalDateTime.of(2026, 10, 15, 0, 0, 0));
    }
}
