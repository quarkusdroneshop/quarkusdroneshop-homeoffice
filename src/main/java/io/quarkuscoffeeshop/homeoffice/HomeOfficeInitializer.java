package io.quarkuscoffeeshop.homeoffice;

import io.quarkus.runtime.Startup;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.StoreLocation;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Startup
@ApplicationScoped
public class HomeOfficeInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeOfficeInitializer.class);

    @ConfigProperty(name = "locations")
    Optional<List<String>> storeLocations;

    @PostConstruct
    @Transactional
    public void initialize() {

        if (storeLocations.isPresent()) {
            loadLocations(storeLocations.get());
        }else{
            loadLocations(new ArrayList<>(){{
                add("ATLANTA");
                add("RALEIGH");
                add("CHARLOTTE");
            }});
        }
    }

    void loadLocations(List<String> locations) {
        locations.stream().map(location -> {
            StoreLocation storeLocation = new StoreLocation(location);
            storeLocation.persist();
            return null;
        });
        LOGGER.info("locations initialized with {}", locations);
    }
}
