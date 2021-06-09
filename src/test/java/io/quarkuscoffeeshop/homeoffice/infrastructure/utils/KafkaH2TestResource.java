package io.quarkuscoffeeshop.homeoffice.infrastructure.utils;

import io.quarkus.test.h2.H2DatabaseTestResource;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;

import java.util.HashMap;
import java.util.Map;

import static io.quarkuscoffeeshop.homeoffice.infrastructure.KafkaTopics.*;

public class KafkaH2TestResource extends H2DatabaseTestResource {

    @Override
    public Map<String, String> start() {
        super.start();
        Map<String, String> env = new HashMap<>();
        Map<String, String> props1 = InMemoryConnector.switchIncomingChannelsToInMemory(ORDERS_CREATED);
        Map<String, String> props2 = InMemoryConnector.switchIncomingChannelsToInMemory(ORDERS_UPDATED);
        Map<String, String> props3 = InMemoryConnector.switchIncomingChannelsToInMemory(LOYALTY_MEMBER_PURCHASE);
        env.putAll(props1);
        env.putAll(props2);
        env.putAll(props3);
        return env;
    }

    @Override
    public void stop() {
        InMemoryConnector.clear();
    }
}
