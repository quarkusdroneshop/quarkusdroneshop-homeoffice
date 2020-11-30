package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkuscoffeeshop.homeoffice.domain.Order;
import io.quarkuscoffeeshop.homeoffice.domain.StoreLocation;
import org.eclipse.microprofile.graphql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;

@GraphQLApi
public class OrdersResource {

    Logger logger = LoggerFactory.getLogger(OrdersResource.class);

    @Inject
    OrderService orderService;

    @Query("allOrders")
    @Description("Get all orders from all stores")
    public List<Order> allOrders() {

        return orderService.allOrders();
    }

    @Query("allOrdersByLocation")
    @Description("Get all orders from a single location")
    public List<Order> allOrdersByLocation(@Name("locationId") StoreLocation storeLocation) {
        return orderService.allOrdersByLocation(storeLocation);
    }


}