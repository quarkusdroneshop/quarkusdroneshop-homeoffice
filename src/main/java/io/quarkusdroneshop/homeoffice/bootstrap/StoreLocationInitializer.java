package io.quarkusdroneshop.homeoffice.bootstrap;

import io.quarkus.runtime.StartupEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.transaction.Transactional;
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