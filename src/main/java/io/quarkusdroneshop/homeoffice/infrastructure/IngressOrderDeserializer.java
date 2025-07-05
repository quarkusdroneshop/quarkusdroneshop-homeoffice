package io.quarkusdroneshop.homeoffice.infrastructure;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.IngressOrder;

public class IngressOrderDeserializer extends ObjectMapperDeserializer<IngressOrder> {

    public IngressOrderDeserializer() {
        super(IngressOrder.class);
    }

}
