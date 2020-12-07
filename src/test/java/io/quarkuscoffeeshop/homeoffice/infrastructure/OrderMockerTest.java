package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkuscoffeeshop.homeoffice.domain.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class OrderMockerTest {

    Logger logger = LoggerFactory.getLogger(OrderMockerTest.class);

    @Inject
    OrderMocker orderMocker;

    @Test
    public void testMockOrders() {
        Order order = orderMocker.mockOrder();
        assertNotNull(order);
        logger.info("{}", order);
        assertTrue(order.getOrderPlacedTimestamp().isBefore(order.getOrderCompletedTimestamp()));
    }
}
