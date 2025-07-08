package io.quarkusdroneshop.homeoffice.infrastructure;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.OrderRecord;

public class OrderRecordDeserializer extends ObjectMapperDeserializer<OrderRecord> {
    public OrderRecordDeserializer() {
        super(OrderRecord.class);
    }
}