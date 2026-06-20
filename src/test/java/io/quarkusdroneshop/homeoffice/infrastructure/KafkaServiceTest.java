package io.quarkusdroneshop.homeoffice.infrastructure;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.OrderRecord;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySource;
import org.junit.jupiter.api.Test;

import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@QuarkusTest
@QuarkusTestResource(KafkaTestResource.class)
public class KafkaServiceTest {

    @InjectSpy
    KafkaService kafkaService;

    @Inject
    @Any
    InMemoryConnector connector;

    private OrderRecord buildOrderRecord() {
        OrderRecord rec = new OrderRecord();
        rec.orderPlacedTimestamp = Instant.now();
        rec.orderCompletedTimestamp = Instant.now();
        return rec;
    }

    @Test
    void testOnOrderCreated_isCalled() {
        InMemorySource<OrderRecord> source = connector.source("orders-created");
        source.send(buildOrderRecord());
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(
                () -> verify(kafkaService).onOrderCreated(any(OrderRecord.class)));
    }

    @Test
    void testOnOrderUpdated_isCalled() {
        InMemorySource<OrderRecord> source = connector.source("orders-updated");
        source.send(buildOrderRecord());
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(
                () -> verify(kafkaService).onOrderUpated(any(OrderRecord.class)));
    }

    @Test
    void testOnLoyaltyMemberPurchase_isCalled() {
        InMemorySource<OrderRecord> source = connector.source("loyalty-member-purchase");
        source.send(buildOrderRecord());
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(
                () -> verify(kafkaService).onLoyaltyMemberPurchase(any(OrderRecord.class)));
    }
}
