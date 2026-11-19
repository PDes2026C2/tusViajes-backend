package ar.edu.unq.tusViajes;

import ar.edu.unq.tusViajes.metrics.TravelPackageMetricsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.TimeZone;

@SpringBootApplication
@EnableConfigurationProperties(TravelPackageMetricsProperties.class)
public class TusViajesApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
		SpringApplication.run(TusViajesApplication.class, args);
	}

}
