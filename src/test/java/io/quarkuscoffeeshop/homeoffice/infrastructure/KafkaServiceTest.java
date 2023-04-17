package io.quarkuscoffeeshop.homeoffice.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkuscoffeeshop.homeoffice.domain.Order;
import io.quarkuscoffeeshop.homeoffice.domain.OrderSource;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.EventType;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.IngressLineItem;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.IngressOrder;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.quarkuscoffeeshop.homeoffice.infrastructure.KafkaTopics.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@QuarkusTest @QuarkusTestResource(KafkaH2TestResource.class)
public class KafkaServiceTest {

    @InjectSpy
    KafkaService kafkaService;

    @Inject
    @Any
    InMemoryConnector connector;

    InMemorySource<Order> orderCreated;

    InMemorySource<Order> orderUpdated;

    InMemorySource<IngressOrder> loyaltyMemberPurchase;

    @BeforeEach
    public void setUp() {
        orderCreated = connector.source(ORDERS_CREATED);
        orderUpdated = connector.source(ORDERS_UPDATED);
        loyaltyMemberPurchase = connector.source(LOYALTY_MEMBER_PURCHASE);
    }

/*
    @Test
    public void testLoyaltyMemberPurchase() {

        IngressOrder ingressOrder = new IngressOrder(
                UUID.randomUUID().toString(),
                OrderSource.COUNTER,
                EventType.LoyaltyMemberPurchase,
                UUID.randomUUID().toString(),
                Instant.now(),
                new ArrayList<IngressLineItem>(){{
                    add(new IngressLineItem());
                }},
                null) {
        );
        loyaltyMemberPurchase.send(ingressOrder);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            assertNull(e);
        }
//        Mockito.verify(kafkaService, Mockito.times(1)).onLoyaltyMemberPurchase(any(IngressOrder.class));
        Mockito.verify(kafkaService,Mockito.times(1)).onLoyaltyMemberPurchase(any(IngressOrder.class));

    }

*/
/*
    @Test
    public void testOrderUpdated() {

        OrderMocker orderMocker = new OrderMocker();
        Order order = orderMocker.mockOrder();
        orderUpdated.send(order);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            assertNull(e);
        }
        Mockito.verify(kafkaService, Mockito.times(1)).onOrderUpated(any(IngressOrder.class));
    }
*//*


*/
/*
    @Test
    public void testOnOrderCreated() {

        OrderMocker orderMocker = new OrderMocker();
        Order order = orderMocker.mockOrder();
        orderCreated.send(order);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            assertNull(e);
        }
        Mockito.verify(kafkaService, Mockito.times(1)).onOrderCreated(any(IngressOrder.class));

    }
*//*

}
*/
