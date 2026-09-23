package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.adapters.dto.CityDTO;
import ar.edu.unq.tusViajes.adapters.dto.CountryDTO;
import ar.edu.unq.tusViajes.adapters.dto.FlightDTO;
import ar.edu.unq.tusViajes.adapters.dto.FlightFilterDTO;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import ar.edu.unq.tusViajes.service.FlightService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FlightService flightService;

    private FlightDTO createSampleFlight(Long id, String airline) {
        CountryDTO country = new CountryDTO("AR", "Argentina");
        CityDTO origin = new CityDTO(1L, "Buenos Aires", country);
        CityDTO destination = new CityDTO(2L, "Bariloche", country);
        return new FlightDTO(
                id,
                airline,
                origin,
                destination,
                LocalDateTime.parse("2026-10-15T08:00:00"),
                LocalDateTime.parse("2026-10-15T10:15:00")
        );
    }

    @Test
    void getAvailableFlights_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/flights"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAvailableFlights_returns403_whenRoleIsBuyer() throws Exception {
        mockMvc.perform(get("/api/flights").with(user("buyer").roles("BUYER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAvailableFlights_returns200AndFlightList_whenRoleIsAgency() throws Exception {
        FlightDTO flight = createSampleFlight(1L, "Aerolíneas Argentinas");
        when(flightService.getAvailableFlights(any(), any(), any())).thenReturn(List.of(flight));

        mockMvc.perform(get("/api/flights").with(user("agency").roles("AGENCY")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].airline").value("Aerolíneas Argentinas"))
                .andExpect(jsonPath("$[0].originCity.name").value("Buenos Aires"))
                .andExpect(jsonPath("$[0].destinationCity.name").value("Bariloche"));
    }

    @Test
    void getAvailableFlights_returns200AndFlightList_whenRoleIsAdmin() throws Exception {
        FlightDTO flight = createSampleFlight(2L, "Flybondi");
        when(flightService.getAvailableFlights(any(), any(), any())).thenReturn(List.of(flight));

        mockMvc.perform(get("/api/flights").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].airline").value("Flybondi"));
    }

    @Test
    void getAvailableFlights_passesFilterParametersCorrectly() throws Exception {
        FlightDTO flight = createSampleFlight(3L, "Flybondi");
        when(flightService.getAvailableFlights(any(), any(), any())).thenReturn(List.of(flight));

        mockMvc.perform(get("/api/flights")
                        .with(user("agency").roles("AGENCY"))
                        .param("airline", "Flybondi")
                        .param("originCityId", "1")
                        .param("destinationCityId", "2")
                        .param("departureDateFrom", "2026-10-15T00:00:00")
                        .param("departureDateTo", "2026-10-15T23:59:59")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3L));

        ArgumentCaptor<FlightFilterDTO> filterCaptor = ArgumentCaptor.forClass(FlightFilterDTO.class);
        ArgumentCaptor<Integer> pageCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> sizeCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(flightService).getAvailableFlights(filterCaptor.capture(), pageCaptor.capture(), sizeCaptor.capture());

        FlightFilterDTO capturedFilter = filterCaptor.getValue();
        assertThat(capturedFilter.airline()).isEqualTo("Flybondi");
        assertThat(capturedFilter.originCityId()).isEqualTo(1L);
        assertThat(capturedFilter.destinationCityId()).isEqualTo(2L);
        assertThat(capturedFilter.departureDateFrom()).isEqualTo(LocalDateTime.parse("2026-10-15T00:00:00"));
        assertThat(capturedFilter.departureDateTo()).isEqualTo(LocalDateTime.parse("2026-10-15T23:59:59"));
        assertThat(pageCaptor.getValue()).isEqualTo(0);
        assertThat(sizeCaptor.getValue()).isEqualTo(10);
    }

    @Test
    void getById_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/flights/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_returns403_whenRoleIsBuyer() throws Exception {
        mockMvc.perform(get("/api/flights/1").with(user("buyer").roles("BUYER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_returns200AndFlight_whenExists_andRoleIsAgency() throws Exception {
        FlightDTO flight = createSampleFlight(10L, "JetSMART");
        when(flightService.getAvailableFlight(10L)).thenReturn(flight);

        mockMvc.perform(get("/api/flights/10").with(user("agency").roles("AGENCY")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.airline").value("JetSMART"));

        verify(flightService).getAvailableFlight(10L);
    }

    @Test
    void getById_returns404_whenFlightDoesNotExist() throws Exception {
        when(flightService.getAvailableFlight(999L)).thenThrow(new ResourceNotFoundException("Flight with id 999 not found"));

        mockMvc.perform(get("/api/flights/999").with(user("agency").roles("AGENCY")))
                .andExpect(status().isNotFound());
    }
}
