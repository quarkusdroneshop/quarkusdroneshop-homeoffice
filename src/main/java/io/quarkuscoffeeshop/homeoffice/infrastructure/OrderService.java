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

    @Transactional
    public void mockAndPersistOrder() {
        mockOrder().persist();
    }

    protected Order mockOrder() {

        Instant orderCompletedTimestamp = Instant.now();
        Instant  orderPlacedTimestamp = orderCompletedTimestamp.minus(makeTime(), ChronoUnit.MINUTES);
        List<LineItem> lineItems = mockLineItems();

        Order order = new Order(UUID.randomUUID().toString(),
                lineItems,
                randomOrderSource(),
                randomLocation(),
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
            }else if(rand % 3 == 0){
                valid = true;
            }
        }
        logger.debug("returning random time of {}", rand);
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
        return new LineItem(item, item.getPrice(), randomCook());
    }

    Item randomBaristaItem() {
        return Item.values()[new Random().nextInt(5)];
    }

    private LineItem mockBeverage() {
        Item item = randomBaristaItem();
        return new LineItem(item, item.getPrice(), randomBarista());
    }

    private Item randomKitchenItem() {
        return Item.values()[new Random().nextInt(3) + 5];
    }

    private final String randomBarista() {
        return randomName(baristas);
    }

    private final String randomCook() {
        return randomName(cooks);
    }

    private String randomName(final List<String> names) {

        Collections.shuffle(names);
        Random rand = new Random();
        return names.get(rand.nextInt(names.size()));
    }

    static final List<String> baristas = Arrays.asList(
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
            "Steve W",
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
            "Drewry M");

    static final List<String> cooks = Arrays.asList(
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
            "Sangem S",
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
            "Babaoglu B",
            "DesPres D",
            "Morris M",
            "iStefan L",
            "Prasanth p",
            "Raghu J",
            "Etienne F",
            "Avik P",
            "Valarie R",
            "smita d",
            "Ram M",
            "Lisa B",
            "Kelvina B",
            "Carlo C",
            "Mario C",
            "Marilyn D",
            "Freddy G",
            "Arvin H",
            "Rajesh K",
            "Alex L",
            "Ned N",
            "Jose N",
            "Jilani P",
            "Henok T",
            "Hilda T",
            "Steve W",
            "Edgar E",
            "Michele J",
            "Anthony G",
            "Gaurav S",
            "Seza B",
            "Partha K",
            "Steve B",
            "Martin a",
            "Lawrence D");


}
