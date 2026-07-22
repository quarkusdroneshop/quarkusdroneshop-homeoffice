package io.quarkusdroneshop.homeoffice.infrastructure;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.OrderRecord;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySource;
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

    @InjectSpy
    OrderAssemblyAggregator orderAssemblyAggregator;

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
    void testOnLineItem_isCalled() {
        InMemorySource<OrderPlacedLineItem> source = connector.source("orders-created");
        OrderPlacedLineItem event = new OrderPlacedLineItem(
                "order-1", "WEB", "ATLANTA", null,
                "item-1", "QDC_A101", "Taro", java.math.BigDecimal.valueOf(135.50), "QDCA10");
        source.send(event);
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(
                () -> verify(orderAssemblyAggregator).onLineItem(any(OrderPlacedLineItem.class)));
    }

    @Test
    void testOnOrderUpdated_isCalled() {
        InMemorySource<OrderUpMessage> source = connector.source("orders-updated");
        OrderUpMessage msg = new OrderUpMessage();
        msg.orderId = "nonexistent-order-id";
        msg.lineItemId = "nonexistent-line-item-id";
        msg.madeBy = "tester";
        source.send(msg);
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(
                () -> verify(kafkaService).onOrderUpated(any(OrderUpMessage.class)));
    }

    @Test
    void testOnLoyaltyMemberPurchase_isCalled() {
        InMemorySource<OrderRecord> source = connector.source("loyalty-member-purchase");
        source.send(buildOrderRecord());
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(
                () -> verify(kafkaService).onLoyaltyMemberPurchase(any(OrderRecord.class)));
    }
}
