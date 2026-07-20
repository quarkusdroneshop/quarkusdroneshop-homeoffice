package io.quarkusdroneshop.homeoffice.infrastructure;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class OrderUpMessageDeserializer extends ObjectMapperDeserializer<OrderUpMessage> {
    public OrderUpMessageDeserializer() {
        super(OrderUpMessage.class);
    }
}
