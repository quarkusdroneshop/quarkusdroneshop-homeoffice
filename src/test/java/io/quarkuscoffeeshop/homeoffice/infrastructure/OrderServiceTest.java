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
public class OrderServiceTest {

    Logger logger = LoggerFactory.getLogger(OrderServiceTest.class);

    @Inject
    OrderService orderService;

    @Test
    public void testMockOrders() {
        Order order = orderService.mockOrder();
        assertNotNull(order);
        logger.info("{}", order);
        assertTrue(order.getOrderPlacedTimestamp().isBefore(order.getOrderCompletedTimestamp()));
    }
}
