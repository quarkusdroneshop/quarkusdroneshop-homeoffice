package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkuscoffeeshop.homeoffice.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class OrderMocker {

    Logger logger = LoggerFactory.getLogger(OrderMocker.class);

    static Map<StoreLocation, List<String>> baristas = new HashMap() {{
        put(
                StoreLocation.ATLANTA,
                Arrays.asList(
                        "Hortelano H",
                        "Chip B",
                        "Lisa B",
                        "Kelvina B",
                        "Carlo C",
                        "Mario C",
                        "Ginny R",
                        "Freddy G",
                        "Arvind K",
                        "Alex L",
                        "Jose N",
                        "Jilani P",
                        "Henok T",
                        "Hilda T",
                        "Steve W"));
        put(
                StoreLocation.CHARLOTTE,
                Arrays.asList(
                        "Edgar E",
                        "Michele J",
                        "Anthony G",
                        "Seza B",
                        "Kelvin R",
                        "Sachin A",
                        "Mohammed L",
                        "Chris S",
                        "David N",
                        "Martin a",
                        "Lawrence D",
                        "Drewry M"));
        put(StoreLocation.RALEIGH,
                Arrays.asList("Edgar E",
                        "Michele J",
                        "Anthony G",
                        "Seza B",
                        "Kelvin R",
                        "Sachin A",
                        "Mohammed L",
                        "Chris S",
                        "David N",
                        "Martin a",
                        "Lawrence D",
                        "Drewry M"));


    }};

    static Map<StoreLocation, List<String>> cooks = new HashMap() {{
        put(
                StoreLocation.ATLANTA,
                Arrays.asList(
                        "Hortelano H",
                        "Chip B",
                        "Lisa B",
                        "Chidambaram C",
                        "Larsson L",
                        "premkumar p",
                        "Jaganaathan J",
                        "Fokou F",
                        "Paul P",
                        "Regas R",
                        "Doshi d",
                        "Maddali M",
                        "Sangem S",
                        "Hilda T",
                        "Steve W"));
        put(
                StoreLocation.CHARLOTTE,
                Arrays.asList(
                        "Mateti M",
                        "Bergh B",
                        "Sinha S",
                        "Sahu S",
                        "Birkenberger B",
                        "Burrell B",
                        "Calingasan C",
                        "Durning D",
                        "Gaitan G",
                        "Gomez P",
                        "Seth S",
                        "Drewry M"));
        put(StoreLocation.RALEIGH,
                Arrays.asList(
                        "Babaoglu B",
                        "DesPres D",
                        "Morris M",
                        "Stefan L",
                        "Etienne F",
                        "Avik P",
                        "Valarie R",
                        "Smita d",
                        "Lisa B",
                        "Kelvina B",
                        "Carlo C",
                        "Mario C",
                        "Marilyn D",
                        "Freddy G"));


    }};

    private List<Order> orders = new ArrayList<>();

    @Transactional
    public void mockAndPersistOrder() {
        mockOrder().persist();
    }

    public OrderMocker() {
    }

    protected Order mockOrder() {

        Instant orderCompletedTimestamp = Instant.now();
        Instant orderPlacedTimestamp = orderCompletedTimestamp.minus(makeTime(), ChronoUnit.MINUTES);
        StoreLocation storeLocation = randomLocation();
        List<LineItem> lineItems = mockLineItems(storeLocation);

        Order order = new Order(UUID.randomUUID().toString(),
                lineItems,
                randomOrderSource(),
                storeLocation.toString(),
                randomCustomerLoyaltyId(),
                orderPlacedTimestamp,
                orderCompletedTimestamp);

        logger.debug("mocked {} ", order);
        return order;
    }

    private long makeTime() {
        boolean valid = false;
        long rand = 1L;
        while (!valid) {
            rand = 1L + (long) (Math.random() * (10L - 1L));
            logger.debug("random time of {}", rand);
            if (rand <= 4) {
                valid = true;
            } else if (rand % 3 == 0) {
                valid = true;
            }
        }
        logger.debug("returning random time of {}", rand);
        return rand;
    }

    private String randomCustomerLoyaltyId() {
        int rand = new Random().nextInt(99) + 1;
        if (rand % 6 == 0) {
            return UUID.randomUUID().toString();
        } else {
            return null;
        }
    }

    private OrderSource randomOrderSource() {
        int rand = new Random().nextInt(99) + 1;
        if (rand % 10 == 0) {
            return OrderSource.WEB;
        } else if (rand % 4 == 0) {
            return OrderSource.PARTNER;
        } else {
            return OrderSource.COUNTER;
        }
    }

    private StoreLocation randomLocation() {
        int rand = new Random().nextInt(3) + 1;
        switch (rand) {
            case 1:
                return StoreLocation.ATLANTA;
            case 2:
                return StoreLocation.CHARLOTTE;
            case 3:
                return StoreLocation.RALEIGH;
            default:
                return StoreLocation.RALEIGH;
        }
    }

    private List<LineItem> mockLineItems(final StoreLocation storeLocation) {

        int numberOfItems = new Random().nextInt(5) + 1;
        List<LineItem> retVal = new ArrayList<>();
        if (numberOfItems % 2 == 0) {
            retVal.addAll(Stream.generate(() -> {
                return mockBeverage(storeLocation);
            }).limit(numberOfItems).collect(Collectors.toList()));
        } else {
            retVal.add(mockBeverage(storeLocation));
        }
        if ((new Random().nextInt(10) % 3) == 0) {
            retVal.addAll(Stream.generate(() -> { return mockKitchenItem(storeLocation);}).limit(numberOfItems).collect(Collectors.toList()));
        }
        ;
        return retVal;
    }

    private LineItem mockKitchenItem(final StoreLocation storeLocation) {
        Item item = randomKitchenItem();
        return new LineItem(item, item.getPrice(), randomCook(storeLocation));
    }

    Item randomBaristaItem() {
        return Item.values()[new Random().nextInt(5)];
    }

    private LineItem mockBeverage(final StoreLocation storeLocation) {
        Item item = randomBaristaItem();
        return new LineItem(item, item.getPrice(), randomBarista(storeLocation));
    }

    private Item randomKitchenItem() {
        return Item.values()[new Random().nextInt(3) + 5];
    }

    private String randomBarista(final StoreLocation storeLocation) {
        return randomName(baristas.get(storeLocation));
    }

    private String randomCook(final StoreLocation storeLocation) {
        return randomName(cooks.get(storeLocation));
    }

    private String randomName(final List<String> names) {

        Collections.shuffle(names);
        Random rand = new Random();
        return names.get(rand.nextInt(names.size()));
    }

}
