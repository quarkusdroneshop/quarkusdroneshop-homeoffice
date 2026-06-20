package io.quarkusdroneshop.homeoffice.infrastructure;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;

import java.util.HashMap;
import java.util.Map;

public class KafkaTestResource implements QuarkusTestResourceLifecycleManager {

    @Override
    public Map<String, String> start() {
        Map<String, String> env = new HashMap<>();
        env.putAll(InMemoryConnector.switchIncomingChannelsToInMemory("orders-created"));
        env.putAll(InMemoryConnector.switchIncomingChannelsToInMemory("orders-updated"));
        env.putAll(InMemoryConnector.switchIncomingChannelsToInMemory("loyalty-member-purchase"));
        env.put("KAFKA_BOOTSTRAP_URLS", "localhost:9092");
        return env;
    }

    @Override
    public void stop() {
        InMemoryConnector.clear();
    }
}
