package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkuscoffeeshop.homeoffice.domain.Order;

public class OrderDeserializer extends ObjectMapperDeserializer<Order> {

    public OrderDeserializer() {
        super(Order.class);
    }
}
