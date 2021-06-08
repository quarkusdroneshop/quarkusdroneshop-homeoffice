package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkuscoffeeshop.homeoffice.domain.Order;
import io.smallrye.reactive.messaging.annotations.Blocking;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import static io.quarkuscoffeeshop.homeoffice.infrastructure.KafkaTopics.ORDERS_CREATED;

@ApplicationScoped
public class KafkaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaService.class);

    @Incoming(ORDERS_CREATED)
    @Blocking
    @Transactional
    public void onOrderCreated(final Order order) {

        LOGGER.debug("Order received: {}", order);
    }
}
