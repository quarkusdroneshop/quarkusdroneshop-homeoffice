package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkuscoffeeshop.homeoffice.domain.Item;
import io.quarkuscoffeeshop.homeoffice.domain.OrderSource;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.EventType;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.LineItemRecord;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.OrderRecord;
import io.quarkuscoffeeshop.homeoffice.infrastructure.utils.KafkaH2TestResource;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.quarkuscoffeeshop.homeoffice.infrastructure.KafkaTopics.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest @QuarkusTestResource(KafkaH2TestResource.class)
public class KafkaServiceTest {

    @InjectSpy
    KafkaService kafkaService;

    @Inject
    @Any
    InMemoryConnector connector;

    InMemorySource<OrderRecord> orderCreated;

    InMemorySource<OrderRecord> orderUpdated;

    InMemorySource<OrderRecord> loyaltyMemberPurchase;

    @BeforeEach
    public void setUp() {
        orderCreated = connector.source(ORDERS_CREATED);
        orderUpdated = connector.source(ORDERS_UPDATED);
        loyaltyMemberPurchase = connector.source(LOYALTY_MEMBER_PURCHASE);
    }

    @Test
    public void testLoyaltyMemberPurchase() {

        List<LineItemRecord> baristaLineItems = null;
        List<LineItemRecord> kitchenLineItems = null;

        OrderRecord orderRecord = mockOrderRecord();

        loyaltyMemberPurchase.send(orderRecord);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            assertNull(e);
        }
        Mockito.verify(kafkaService,Mockito.times(1)).onLoyaltyMemberPurchase(any(OrderRecord.class));

    }

    private static OrderRecord mockOrderRecord() {
        OrderRecord orderRecord = new OrderRecord(
                UUID.randomUUID().toString(),
                OrderSource.COUNTER,
                EventType.OrderCreated,
                null,
                Instant.now(),
                new ArrayList<>(){{
                    add(new LineItemRecord(Item.CAPPUCCINO, "Capt. Kirk"));
                }},
                null);
        return orderRecord;
    }

    @Test
    public void testOrderUpdated() {

        orderUpdated.send(mockOrderRecord());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            assertNull(e);
        }
        Mockito.verify(kafkaService, Mockito.times(1)).onOrderUpated(any(OrderRecord.class));
    }

    @Test
    public void testOnOrderCreated() {

        orderCreated.send(mockOrderRecord());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            assertNull(e);
        }
        Mockito.verify(kafkaService, Mockito.times(1)).onOrderCreated(any(OrderRecord.class));

    }

}
