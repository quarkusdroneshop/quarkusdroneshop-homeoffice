package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkuscoffeeshop.homeoffice.domain.Order;
import io.quarkuscoffeeshop.homeoffice.domain.StoreLocation;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;

@ApplicationScoped
public class OrderService {

    public List<Order> allOrders() {
        return Order.listAll();
    }

    public List<Order> allOrdersByLocation(final StoreLocation storeLocation) {
        return Order.find("locationId = :locationId", new HashMap<String, Object>(){{ put("locationId", storeLocation); }}).list();

    }
}
