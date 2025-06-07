package io.quarkuscoffeeshop.homeoffice.bootstrap;

import io.quarkus.runtime.StartupEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.transaction.Transactional;
import io.quarkuscoffeeshop.homeoffice.domain.Store;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.StoreLocation;

@ApplicationScoped
public class StoreLocationInitializer {

    @Transactional
    void onStart(@Observes StartupEvent event) {
        for (Store location : Store.values()) {
            String loc = location.name();
            StoreLocation existing = StoreLocation.find("location", loc).firstResult();
            if (existing == null) {
                new StoreLocation(loc).persist();
            }
        }
    }
}