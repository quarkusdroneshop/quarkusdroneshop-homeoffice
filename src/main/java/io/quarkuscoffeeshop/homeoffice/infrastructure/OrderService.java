package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkuscoffeeshop.homeoffice.domain.Item;
import io.quarkuscoffeeshop.homeoffice.domain.LineItem;
import io.quarkuscoffeeshop.homeoffice.domain.Order;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class OrderService {

    private List<Order> orders = new ArrayList<>();

    @PostConstruct
    private void setUp() {
        orders.addAll(Stream.generate(this::mockOrder).limit(100).collect(Collectors.toList()));
    }

    public List<Order> allOrders() {
        return orders;
    }

    private Order mockOrder() {

        List<LineItem> lineItems = mockLineItems();
        BigDecimal sum = lineItems
                .stream()
                .map(item -> item.getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Order(UUID.randomUUID().toString(), lineItems, sum);
    }

    private List<LineItem> mockLineItems() {

        int numberOfItems = new Random().nextInt(5) + 1;
        List<LineItem> retVal = Stream.generate(this::mockBeverage).limit(numberOfItems).collect(Collectors.toList());
        if((new Random().nextInt(10) % 3)==0){
            retVal.addAll(Stream.generate(this::mockKitchenItem).limit(numberOfItems).collect(Collectors.toList()));
        };
        return retVal;
    }

    private LineItem mockBeverage() {
        Item item = randomBaristaItem();
        return new LineItem(UUID.randomUUID().toString(), item, item.getPrice());
    }

    private LineItem mockKitchenItem() {
        Item item = randomKitchenItem();
        return new LineItem(UUID.randomUUID().toString(), item, item.getPrice());
    }

    Item randomBaristaItem() {
        return Item.values()[new Random().nextInt(5)];
    }

    Item randomKitchenItem() {
        return Item.values()[new Random().nextInt(3) + 5];
    }

}
