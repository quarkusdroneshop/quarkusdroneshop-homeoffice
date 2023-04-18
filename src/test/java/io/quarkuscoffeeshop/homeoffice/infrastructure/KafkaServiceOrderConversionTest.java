package io.quarkuscoffeeshop.homeoffice.infrastructure;


import io.quarkuscoffeeshop.homeoffice.domain.Item;
import io.quarkuscoffeeshop.homeoffice.domain.Order;
import io.quarkuscoffeeshop.homeoffice.domain.OrderSource;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.EventType;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.IngressLineItem;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.IngressOrder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KafkaServiceOrderConversionTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaServiceOrderConversionTest.class);

    @Test
    public void testUnmarshallingIngressOrder() {
        String ingressOrderJson = "{\"orderId\":\"894d08e2-eb52-4f44-9eca-10d82207d752\",\"orderSource\":\"WEB\",\"eventType\":\"OrderCreated\",\"loyaltyMemberId\":null,\"timestamp\":1623334005.368963000,\"baristaLineItems\":[{\"item\":\"COFFEE_BLACK\",\"name\":\"Jeremy\"}],\"kitchenLineItems\":null}";

        IngressOrderDeserializer ingressOrderDeserializer = new IngressOrderDeserializer();
        Object result = ingressOrderDeserializer.deserialize("orders-created", ingressOrderJson.getBytes(StandardCharsets.UTF_8));
        assertNotNull(result);
        System.out.println(result.toString());
    }

//    @Test
//    public void testOrderConversion() {
//        List<IngressLineItem> baristaLineItems = mockBaristaLineItems();
//        IngressOrder ingressOrder = new IngressOrder(
//                UUID.randomUUID().toString(),
//                OrderSource.WEB,
//                EventType.OrderCreated,
//                null,
//                Instant.now(),
//                baristaLineItems,
//                null
//        );
//
//        KafkaService kafkaService = new KafkaService();
//        Order order = kafkaService.convertIngressOrderToOrder(ingressOrder);
//        LOGGER.debug("Order: {}", order);
//        System.out.println(order.toString());
//        assertNotNull(order);
//        assertEquals(1, order.getLineItems().size());
//
//    }

    private List<IngressLineItem> mockBaristaLineItems() {
        return new ArrayList<>(Arrays.asList(new IngressLineItem(Item.ESPRESSO, "Barney", null)));
    }

}
