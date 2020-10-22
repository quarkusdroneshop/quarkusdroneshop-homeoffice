package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkuscoffeeshop.homeoffice.domain.Order;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.wildfly.common.Assert.assertNotNull;

@QuarkusTest
public class OrderServiceTest {

    @Inject
    OrderService orderService;

    @Test
    public void testMockOrders() {
        List<Order> orders = orderService.allOrders();
        assertNotNull(orders);
        assertEquals(100, orders.size());
        orders.forEach(order -> {
            System.out.println(order.toString());
        });
    }
}
