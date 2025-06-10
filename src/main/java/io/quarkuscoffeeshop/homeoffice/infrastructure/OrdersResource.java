package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkus.panache.common.Parameters;
import io.quarkuscoffeeshop.homeoffice.domain.*;
import io.quarkuscoffeeshop.homeoffice.viewmodels.*;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@GraphQLApi
public class OrdersResource {

    Logger logger = LoggerFactory.getLogger(OrdersResource.class);

    @Inject
    OrderService orderService;
    
    /*
    Example Query
    {
    "query": "query orders { ordersForLocation(location: \"TOKYO\") { orderId location lineItems { id item price preparedBy } total orderPlacedTimestamp orderCompletedTimestamp } }"
    }
     */
    @Query
    @Description("Get all orders from store by location")
    public List<Order> ordersForLocation(String location) {
        return Order.list("location", location);
    }

    @Query
    public List<LocationOrders> getOrdersByLocation() {
        List<LocationOrders> aggregate = new ArrayList<>();
        for (Store location : Store.values()) {
            List<Order> locationOrders =  Order.list("location", location.name());
            aggregate.add(new LocationOrders(location.name(), locationOrders));
        }
        return aggregate;
    }

    @Query
    public List<ItemSales> getItemSales(){
        List<ItemSales> sales = new ArrayList<>();

        for (Item item : Item.values()) {
            long soldItems = LineItem.count("item", item);
            BigDecimal price = item.getPrice();
            ItemSales itemSales = new ItemSales();
            itemSales.item = item;
            itemSales.salesTotal = soldItems;
            itemSales.revenue = price.multiply(BigDecimal.valueOf(itemSales.salesTotal)).doubleValue();
            sales.add(itemSales);
        }
        return sales;
    }

    /*
    Example Query(データを登録します)
    {
    "query": "query productSalesByDate { productSalesByDate(startDate: \"2025-06-01\", endDate: \"2025-06-30\") { item productItemSales { item date revenue salesTotal } } }"
    }
    */
    @Transactional
    @Query
    public List<ProductSales> getProductSalesByDate(String startDate, String endDate){
        Instant functionStart = Instant.now();
        Instant start = Instant.parse(startDate + "T00:00:00Z");
        Instant end = Instant.parse(endDate + "T00:00:00Z").plus(1, ChronoUnit.DAYS);

        ProductSales lastProductSales = ProductSales.find("order by id desc").firstResult();
        List<Order> orders = new ArrayList<Order>();
        if (lastProductSales != null){
            orders = Order.findBetweenAfter(start, end, lastProductSales.createdTimestamp);
        }else{
            orders = Order.findBetween(start, end);
        }
        //List<Order> orders = Order.findBetween(start, end);
        //logger.debug("Searching orders between: {} and {} in getItemSalesByDate - orders.size():{}", start, end, orders.size());


        List<Instant> dateRange = getDatesBetween(start,end);

        List<ProductSales> productSalesList = new ArrayList<>();

        for (Item item : Item.values()) {
            ProductSales productSales = ProductSales.findByItem(item);

            List<Order> ordersWithProduct = orders.stream().filter(
                    order -> order.getLineItems().stream().filter(
                            lineItem -> lineItem.getItem().equals(item)).count() > 0)
                    .collect(Collectors.toList());

            dateRange.forEach(instant -> {

                //get the orders for that day from the existing collection
                List<Order> ordersForDay = ordersWithProduct.stream().filter(order -> {
                    Instant analysisDate = instant.truncatedTo(ChronoUnit.DAYS);
                    Instant orderDate = order.getOrderPlacedTimestamp().truncatedTo(ChronoUnit.DAYS);
                    return analysisDate.equals(orderDate);
                }).collect(Collectors.toList());

                //logger.debug("getItemSalesByDate - day: {} orders: {}", instant, ordersForDay.size());

                //get the line items for each order
                List<LineItem> lineItemsForDay = new ArrayList<>();
                for( Order order : ordersForDay){
                    List<LineItem> items = order.getLineItems().stream().filter(lineItem -> lineItem.getItem().equals(item)).collect(Collectors.toList());
                    lineItemsForDay.addAll(items);
                }

                long soldItems = lineItemsForDay.stream().filter(lineItem -> lineItem.getItem().equals(item)).count();

                ProductItemSales itemSales = ProductItemSales.find("item = :item AND date = :date",
                        Parameters.with("item", item).and("date", instant)
                ).firstResult();

                if (itemSales != null){
                    //update existing object

                    itemSales.salesTotal = itemSales.salesTotal + soldItems;
                    itemSales.revenue = item.getPrice().multiply(BigDecimal.valueOf(itemSales.salesTotal));
                } else {
                    itemSales = new ProductItemSales();
                    itemSales.item = item;
                    itemSales.salesTotal = soldItems;
                    itemSales.date = instant;
                    itemSales.revenue = item.getPrice().multiply(BigDecimal.valueOf(itemSales.salesTotal));
                    productSales.productItemSales.add(itemSales);
                }
            });
            productSales.createdTimestamp = functionStart;
            productSales.persist();
            productSalesList.add(productSales);
        }

        Instant functionEnd = Instant.now();
        System.out.println("getProductSalesByDate: " + Duration.between(functionStart, functionEnd));
        productSalesList.sort(Comparator.comparing(ProductSales::getItem));
        return productSalesList;
    }

    public static List<Instant> getDatesBetween(Instant startDate, Instant endDate) {

        long numOfDaysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        return IntStream.iterate(0, i -> i + 1)
                .limit(numOfDaysBetween)
                .mapToObj(i -> startDate.plus(i, ChronoUnit.DAYS))
                .collect(Collectors.toList());
    }

    /*
    {
    "query": "query itemSalesTotalsByDate { itemSalesTotalsByDate(startDate: \"2025-01-01\", endDate: \"2025-12-31\") { item revenue salesTotal } }"
    }
     */
    @Query
    public List<ItemSales> getItemSalesTotalsByDate(String startDate, String endDate){
        Instant functionStart = Instant.now();
        Instant start = Instant.parse(startDate + "T00:00:00Z");
        Instant end = Instant.parse(endDate + "T00:00:00Z").plus(1, ChronoUnit.DAYS);
        List<Order> orders = Order.findBetween(start, end);

        List<LineItem> lineItems = new ArrayList<>();
        for( Order order : orders){
            lineItems.addAll(order.getLineItems());
        }

        List<ItemSales> sales = new ArrayList<>();

        for (Item item : Item.values()) {
            List<LineItem> soldItems = lineItems.stream().filter(i -> i.getItem().name().equals(item.name())).collect(Collectors.toList());

            ItemSales itemSales = new ItemSales();
            itemSales.item = item;
            itemSales.salesTotal = soldItems.size();
            itemSales.revenue = item.getPrice().multiply(BigDecimal.valueOf(itemSales.salesTotal)).doubleValue();
            sales.add(itemSales);
        }
        sales.sort((itemSales, t1) -> itemSales.item.name().compareTo(t1.item.name()));
        Instant functionEnd = Instant.now();
        System.out.println("getItemSalesTotalsByDate: " + Duration.between(functionStart, functionEnd));
        return sales;
    }

    /*
    {
    "query": "query { storeServerSales { server store sales { item salesTotal revenue } } }"
    }
     */
    @Query
    public List<StoreServerSales> getStoreServerSales(){
        //I have to come document this - a lot of Hashtable work to get a count of unique items sold by servers by location
        List<StoreServerSales> storeServerSalesList = new ArrayList<>();

        for (Store location : Store.values()) {

            Map<String, Map<Item, ItemSales>> servers = new HashMap<>();


            //get an array of all lineItems for the location
            //this is so much easier using LINQ with entity framework in C#
            List<LineItem> locationLineItems = new ArrayList<>();
            List<Order> orders = Order.list("location", location.name());
            for( Order order : orders){
                locationLineItems.addAll(order.getLineItems());
            }

            //logger.debug("Location: {} : lineItems {}", location.name(), locationLineItems.size() );

            for (LineItem lineItem : locationLineItems){
               if (servers.containsKey(lineItem.getPreparedBy())){
                   //logger.debug("servers contains key: {}",lineItem.getPreparedBy());

                   Hashtable items = (Hashtable) servers.get(lineItem.getPreparedBy());

                   if (items.containsKey(lineItem.getItem())){
                       //update
                       ItemSales itemSales = (ItemSales) items.get(lineItem.getItem());
                       itemSales.salesTotal  = itemSales.salesTotal + 1;
                       itemSales.revenue = itemSales.revenue + lineItem.getPrice().doubleValue();
                       items.put(lineItem.getItem(), itemSales);
                   }else{
                       //new
                       ItemSales itemSales = new ItemSales(lineItem.getItem(), 1, lineItem.getPrice().doubleValue());
                       items.put(lineItem.getItem(), itemSales);
                   }
                   servers.put(lineItem.getPreparedBy(),items);

               }else{
                   Hashtable items = new Hashtable();
                   ItemSales itemSales = new ItemSales(lineItem.getItem(), 1, lineItem.getPrice().doubleValue());
                   items.put(lineItem.getItem(), itemSales);

                   //logger.debug("Adding to core - item: {}, array: {}",lineItem.getPreparedBy(), items.size());
                   servers.put(lineItem.getPreparedBy(), items);
               }
            }

            servers.forEach((key, value)->{
                String server = (String) key;
                Hashtable itemSalesHashTable = (Hashtable) servers.get(key);

                StoreServerSales sales = new StoreServerSales();
                sales.store = location.name();
                sales.server = server;

                List<ItemSales> itemSales = new ArrayList<>();
                itemSalesHashTable.forEach((k, v)->{
                    itemSales.add((ItemSales) v);
                });
                storeServerSalesList.add(sales);
            });

        }

        //logger.debug("stores: " + storeServerSalesList.size());
        return storeServerSalesList;
    }


    /*
    {
    "query": "query { storeServerSalesByDate(startDate: \"2025-01-01\", endDate: \"2025-12-31\") { server store sales { item salesTotal revenue } } }"
    }
     */
    @Query
    public List<StoreServerSales> getStoreServerSalesByDate(String startDate, String endDate){
        Instant functionStart = Instant.now();

        List<StoreServerSales> storeServerSalesList = new ArrayList<>();

        Instant start = Instant.parse(startDate + "T00:00:00Z");
        Instant end = Instant.parse(endDate + "T00:00:00Z").plus(1, ChronoUnit.DAYS);
        List<Order> allOrders = Order.findBetween(start, end);

        for (Store store : Store.values()) {

            Map<String, Map<Item, ItemSales>> servers = new HashMap<>();

            // Store に紐づく注文を抽出
            List<Order> orders = allOrders.stream()
                .filter(o -> store.name().equals(o.getLocation()))
                .collect(Collectors.toList());

            List<LineItem> lineItems = orders.stream()
                .flatMap(order -> order.getLineItems().stream())
                .collect(Collectors.toList());

            for (LineItem lineItem : lineItems){
                String preparedBy = lineItem.getPreparedBy();
                Item item = lineItem.getItem();

                servers.computeIfAbsent(preparedBy, k -> new HashMap<>());

                Map<Item, ItemSales> itemMap = servers.get(preparedBy);

                itemMap.compute(item, (key, existing) -> {
                    if (existing == null) {
                        return new ItemSales(item, 1L, lineItem.getPrice().doubleValue());
                    } else {
                        existing.salesTotal += 1;
                        existing.revenue = existing.revenue + lineItem.getPrice().doubleValue();
                        return existing;
                    }
                });
            }

            for (Map.Entry<String, Map<Item, ItemSales>> entry : servers.entrySet()) {
                StoreServerSales sales = new StoreServerSales();
                sales.store = store.name();
                sales.server = entry.getKey();
                //sales.sales = new ArrayList<>(entry.getValue().values());
                storeServerSalesList.add(sales);
            }
        }

        Instant functionEnd = Instant.now();
        System.out.println("getStoreServerSalesByDate: " + Duration.between(functionStart, functionEnd));
        return storeServerSalesList;
    }

    @Transactional
    @Query
    public int getAverageOrderUpTime(String startDate, String endDate){
        Instant now = Instant.now();
        Instant start = Instant.parse(startDate + "T00:00:00Z");
        Instant end = Instant.parse(endDate + "T00:00:00Z").plus(1, ChronoUnit.DAYS);
        AverageOrderUpTime averageOrderUpTime = AverageOrderUpTime.find("order by id desc").firstResult();
        List<Order> orders = new ArrayList<Order>();
        if (averageOrderUpTime != null){
            orders = Order.findBetweenAfter(start, end, averageOrderUpTime.calculatedAt);
        }else{
            orders = Order.findBetween(start, end);
        }

        long totalTime = 0;
        for( Order order : orders){
            Duration orderDuration = Duration.between(order.getOrderPlacedTimestamp(), order.getOrderCompletedTimestamp());
            totalTime += orderDuration.getSeconds();
        }

        if (orders.size() == 0 || totalTime == 0){
            return 0;
        }else{
            //logger.debug("totalTime: " + totalTime + " orders.size():" + orders.size());
            if (averageOrderUpTime == null){
                int averageTime = (int)(totalTime / orders.size());
                averageOrderUpTime = new AverageOrderUpTime();
                averageOrderUpTime.averageTime = averageTime;
                averageOrderUpTime.orderCount = (int) orders.stream().count();
                averageOrderUpTime.calculatedAt = now;
                averageOrderUpTime.persist();
            }else{
                int oldTotalTime = averageOrderUpTime.averageTime * averageOrderUpTime.orderCount;
                averageOrderUpTime.averageTime = (int)((totalTime + oldTotalTime) / (averageOrderUpTime.orderCount + orders.size()));
                averageOrderUpTime.orderCount = averageOrderUpTime.orderCount + (int) orders.stream().count();
                averageOrderUpTime.calculatedAt = now;
                averageOrderUpTime.persist();
            }
            Instant functionEnd = Instant.now();
            System.out.println("getAverageOrderUpTime: " + Duration.between(now, functionEnd));
            return averageOrderUpTime.averageTime;
        }
    }
}