package rs.ac.uns.ftn.eventhub.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir}")
    private String uploadDir;

    // Frontend i backend rade na razlicitim portovima, pa je CORS dozvoljen na jednom
    // mestu umesto anotacijom nad svakim kontrolerom
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("http://localhost:4200");
    }

    // Otpremljene slike se serviraju sa diska, da bi bile dostupne preko putanje iz baze.
    // Zavrsna kosa crta je obavezna, inace Spring putanju shvata kao fajl a ne kao folder.
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadDir).toAbsolutePath().toUri().toString();
        if (!location.endsWith("/"))
            location = location + "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
