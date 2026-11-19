package ar.edu.unq.tusViajes.cucumber.steps;

import ar.edu.unq.tusViajes.builder.*;
import ar.edu.unq.tusViajes.cucumber.ScenarioContext;
import ar.edu.unq.tusViajes.model.*;
import ar.edu.unq.tusViajes.repository.*;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor
public class CommonSteps {

    private final ScenarioContext scenarioContext;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final HotelRepository hotelRepository;
    private final FlightRepository flightRepository;
    private final AdminRepository adminRepository;
    private final AgencyRepository agencyRepository;
    private final BuyerRepository buyerRepository;
    private final PasswordEncoder passwordEncoder;

    @Dado("que existe el país {string} con código ISO {string}")
    @Dado("existe el país {string} con código ISO {string}")
    public void queExisteElPaisConCodigoIso(String countryName, String isoCode) {
        Country country = countryRepository.save(
                CountryBuilder.aCountry().withName(countryName).withIsoCode(isoCode).build()
        );
        scenarioContext.getCountries().put(countryName, country);
    }

    @Dado("que existe la ciudad {string} en el país {string}")
    @Dado("existe la ciudad {string} en el país {string}")
    public void existeLaCiudadEnElPais(String cityName, String countryName) {
        Country country = scenarioContext.getCountries().get(countryName);
        City city = cityRepository.save(
                CityBuilder.aCity().withName(cityName).withCountry(country).build()
        );
        scenarioContext.getCities().put(cityName, city);
    }

    @Dado("que existe el hotel {string} en la ciudad {string}")
    @Dado("existe el hotel {string} en la ciudad {string}")
    public void existeElHotelEnLaCiudad(String hotelName, String cityName) {
        City city = scenarioContext.getCities().get(cityName);
        Hotel hotel = hotelRepository.save(
                HotelBuilder.aHotel().withName(hotelName).withCity(city).build()
        );
        scenarioContext.getHotels().put(hotelName, hotel);
    }

    @Dado("que existe el vuelo {long} de {string} desde {string} hacia {string}")
    @Dado("existe el vuelo {long} de {string} desde {string} hacia {string}")
    public void existeElVueloDesdeHacia(Long flightId, String airline, String originCityName, String destCityName) {
        City origin = scenarioContext.getCities().get(originCityName);
        City dest = scenarioContext.getCities().get(destCityName);
        Flight flight = flightRepository.save(
                FlightBuilder.aFlight()
                        .withId(flightId)
                        .withAirline(airline)
                        .withOriginCity(origin)
                        .withDestinationCity(dest)
                        .build()
        );
        scenarioContext.getFlights().put(String.valueOf(flightId), flight);
    }

    @Dado("que existe un administrador registrado con email {string} y contraseña {string}")
    @Dado("existe un administrador registrado con email {string} y contraseña {string}")
    public void queExisteUnAdministradorRegistrado(String email, String password) {
        Admin admin = adminRepository.save(
                AdminBuilder.anAdmin()
                        .withEmail(email)
                        .withPasswordHash(passwordEncoder.encode(password))
                        .build()
        );
        scenarioContext.getAdmins().put(email, admin);
    }

    @Dado("que existe un administrador autenticado con email {string}")
    @Dado("existe un administrador autenticado con email {string}")
    public void queExisteUnAdministradorAutenticado(String email) {
        Admin admin = adminRepository.save(
                AdminBuilder.anAdmin()
                        .withEmail(email)
                        .withPasswordHash(passwordEncoder.encode("AdminPass123"))
                        .build()
        );
        scenarioContext.getAdmins().put(email, admin);
        scenarioContext.setCurrentUser(admin);
    }

    @Dado("que existe una agencia con razón social {string}, CUIT {string}, email {string}, contraseña {string} y estado {string}")
    @Dado("existe una agencia con razón social {string}, CUIT {string}, email {string}, contraseña {string} y estado {string}")
    public void queExisteUnaAgenciaConEstado(String businessName, String taxId, String email, String password, String status) {
        Agency agency = agencyRepository.save(
                AgencyBuilder.anAgency()
                        .withBusinessName(businessName)
                        .withTaxId(taxId)
                        .withEmail(email)
                        .withPasswordHash(passwordEncoder.encode(password))
                        .withStatus(AgencyStatus.valueOf(status))
                        .build()
        );
        scenarioContext.getAgencies().put(businessName, agency);
        scenarioContext.getAgencies().put(email, agency);
    }

    @Dado("que existe una agencia autenticada con razón social {string}, CUIT {string} y email {string}")
    @Dado("existe una agencia autenticada con razón social {string}, CUIT {string} y email {string}")
    public void queExisteUnaAgenciaAutenticada(String businessName, String taxId, String email) {
        Agency agency = agencyRepository.save(
                AgencyBuilder.anAgency()
                        .withBusinessName(businessName)
                        .withTaxId(taxId)
                        .withEmail(email)
                        .withPasswordHash(passwordEncoder.encode("AgencyPass123"))
                        .withStatus(AgencyStatus.valueOf(statusAuthorized()))
                        .build()
        );
        scenarioContext.getAgencies().put(businessName, agency);
        scenarioContext.getAgencies().put(email, agency);
        scenarioContext.setCurrentUser(agency);
    }

    private String statusAuthorized() {
        return AgencyStatus.AUTHORIZED.name();
    }

    @Dado("que existe un comprador autenticado llamado {string} {string} con email {string} y DNI {string}")
    @Dado("existe un comprador autenticado llamado {string} {string} con email {string} y DNI {string}")
    public void existeUnCompradorAutenticado(String firstName, String lastName, String email, String nationalId) {
        Buyer buyer = buyerRepository.save(
                BuyerBuilder.aBuyer()
                        .withFirstName(firstName)
                        .withLastName(lastName)
                        .withEmail(email)
                        .withNationalId(nationalId)
                        .withPasswordHash(passwordEncoder.encode("BuyerPass123"))
                        .build()
        );
        scenarioContext.getBuyers().put(email, buyer);
        scenarioContext.setCurrentUser(buyer);
    }

    @Entonces("el código de respuesta HTTP debe ser {int}")
    public void elCodigoDeRespuestaHttpDebeSer(int expectedStatus) throws Exception {
        scenarioContext.getLastResultActions().andExpect(status().is(expectedStatus));
    }

    @Entonces("el mensaje de error de la respuesta es {string}")
    public void elMensajeDeErrorDeLaRespuestaEs(String expectedMessage) throws Exception {
        scenarioContext.getLastResultActions().andExpect(jsonPath("$.message").value(expectedMessage));
    }

    @Entonces("el mensaje de error de la respuesta contiene {string}")
    public void elMensajeDeErrorDeLaRespuestaContiene(String expectedSubstring) throws Exception {
        scenarioContext.getLastResultActions().andExpect(jsonPath("$.message", containsString(expectedSubstring)));
    }
}
