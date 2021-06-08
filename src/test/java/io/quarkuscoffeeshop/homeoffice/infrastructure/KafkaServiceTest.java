package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkuscoffeeshop.homeoffice.domain.Order;
import io.quarkuscoffeeshop.homeoffice.infrastructure.utils.KafkaH2TestResource;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

import static io.quarkuscoffeeshop.homeoffice.infrastructure.KafkaTopics.ORDERS_CREATED;
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

    InMemorySource<Order> ordersCreated;

    @BeforeEach
    public void setUp() {
        ordersCreated = connector.source(ORDERS_CREATED);
    }

    @Test
    public void testReceivingMessages() {

        OrderMocker orderMocker = new OrderMocker();
        Order order = orderMocker.mockOrder();
        ordersCreated.send(order);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            assertNull(e);
        }
        Mockito.verify(kafkaService, Mockito.times(1)).onOrderCreated(any(Order.class));

    }
}
