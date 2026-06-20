package io.quarkusdroneshop.homeoffice.bootstrap;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import io.quarkusdroneshop.homeoffice.domain.Store;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.StoreLocation;

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