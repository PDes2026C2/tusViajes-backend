package ar.edu.unq.tusViajes.cucumber;

import ar.edu.unq.tusViajes.model.*;
import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.Map;

@Component
@ScenarioScope
@Getter
@Setter
public class ScenarioContext {

    private ResultActions lastResultActions;
    private MvcResult lastMvcResult;
    private User currentUser;
    private String lastAccessToken;
    private String lastRefreshToken;

    private final Map<String, Country> countries = new HashMap<>();
    private final Map<String, City> cities = new HashMap<>();
    private final Map<String, Hotel> hotels = new HashMap<>();
    private final Map<String, Flight> flights = new HashMap<>();
    private final Map<String, Agency> agencies = new HashMap<>();
    private final Map<String, Buyer> buyers = new HashMap<>();
    private final Map<String, Admin> admins = new HashMap<>();
    private final Map<String, TravelPackage> travelPackages = new HashMap<>();

    public void recordResult(ResultActions actions) throws Exception {
        this.lastResultActions = actions;
        this.lastMvcResult = actions.andReturn();
    }
}
