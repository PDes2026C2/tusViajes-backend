package ar.edu.unq.tusViajes.cucumber;

import ar.edu.unq.tusViajes.service.FlightsApiService;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
public class CucumberSpringConfiguration {

    @MockitoBean
    private FlightsApiService flightsApiService;
}
