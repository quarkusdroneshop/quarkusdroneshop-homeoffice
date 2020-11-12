package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkuscoffeeshop.homeoffice.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class OrderService {

    Logger logger = LoggerFactory.getLogger(OrderService.class);

    private List<Order> orders = new ArrayList<>();

/*
    @PostConstruct
    private void setUp() {
        orders.addAll(Stream.generate(this::mockOrder).limit(100).collect(Collectors.toList()));
    }

    public List<Order> allOrders() {
        return orders;
    }

*/
    @Transactional
    public void mockOrder() {

        Instant orderCompletedTimestamp = Instant.now();
        Instant orderPlacedTimestamp = orderCompletedTimestamp.minus(makeTime(), ChronoUnit.MINUTES);
        List<LineItem> lineItems = mockLineItems();

/*
        order.setId(UUID.randomUUID().toString());
        order.setLineItems(lineItems);
        order.setLocationId(randomLocation());
        order.setOrderPlacedTimestamp(orderPlacedTimestamp);
        order.setOrderCompletedTimestamp(orderCompletedTimestamp);
        order.setCustomerLoyaltyId(randomCustomerLoyaltyId());
*/

        Order order = new Order(UUID.randomUUID().toString(),
                    lineItems,
                    randomOrderSource(),
                    randomLocation(),
                    randomCustomerLoyaltyId(),
                    orderPlacedTimestamp,
                    orderCompletedTimestamp);

        logger.debug("persisting {} ", order);
        order.persist();
    }

    private long makeTime() {
       boolean valid = false;
       long rand = 1L;
        while (!valid) {
            rand = 1L + (long) (Math.random() * (1L - 10L));
            if (rand <= 4) {
                valid = true;
            }else if(rand % 3 == 0){
                valid = true;
            }
        }
        return rand;
    }

    private String randomCustomerLoyaltyId() {
        int rand = new Random().nextInt(99) + 1;
        if(rand % 6 == 0){
            return UUID.randomUUID().toString();
        }else{
            return null;
        }
    }

    private OrderSource randomOrderSource() {
        int rand = new Random().nextInt(99) + 1;
        if (rand % 10 == 0) {
            return OrderSource.WEB;
        } else if (rand % 4 == 0) {
            return OrderSource.PARTNER;
        }else {
            return OrderSource.COUNTER;
        }
    }

    private String randomLocation() {
        int rand = new Random().nextInt(2) + 1;
        switch (rand) {
            case 1:
                return Locations.ATLANTA.toString();
            case 2:
                return Locations.CHARLOTTE.toString();
            case 3:
                return Locations.RALEIGH.toString();
            default:
                return Locations.RALEIGH.toString();
        }
    }


    private List<LineItem> mockLineItems(){

        int numberOfItems = new Random().nextInt(5) + 1;
        List<LineItem> retVal = new ArrayList<>();
        if(numberOfItems % 2 == 0){
            retVal.addAll(Stream.generate(this::mockBeverage).limit(numberOfItems).collect(Collectors.toList()));
        }else{
            retVal.add(mockBeverage());
        }
        if((new Random().nextInt(10) % 3)==0){
            retVal.addAll(Stream.generate(this::mockKitchenItem).limit(numberOfItems).collect(Collectors.toList()));
        };
        return retVal;
    }

    private LineItem mockKitchenItem() {
        Item item = randomKitchenItem();
        return new LineItem(item, item.getPrice());
    }

    Item randomBaristaItem() {
        return Item.values()[new Random().nextInt(5)];
    }

    private LineItem mockBeverage() {
        Item item = randomBaristaItem();
        return new LineItem(item, item.getPrice());
    }

    private Item randomKitchenItem() {
        return Item.values()[new Random().nextInt(3) + 5];
    }
}
