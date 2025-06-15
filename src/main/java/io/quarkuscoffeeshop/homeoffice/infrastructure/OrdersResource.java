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
    query orders {
        ordersForLocation(location: "TOKYO") {
            orderId
            location
            lineItems {
            id
            item
            price
            preparedBy
            }
            total
            orderPlacedTimestamp
            orderCompletedTimestamp
        }
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
    query productSalesByDate {
        productSalesByDate(startDate: "2025-06-01", endDate: "2025-06-30") {
            item
            productItemSales {
            item
            saleDate
            revenue
            salesTotal
            }
        }
    }
    */
    @Transactional
    @Query
    public List<ProductSales> getProductSalesByDate(String startDate, String endDate) {
        Instant functionStart = Instant.now();
        Instant start = Instant.parse(startDate + "T00:00:00Z");
        Instant end = Instant.parse(endDate + "T00:00:00Z").plus(1, ChronoUnit.DAYS);
    
        //ProductSales lastProductSales = ProductSales.find("order by id desc").firstResult();
        // ProductSales lastProductSales = ProductSales.find(
        // "SELECT ps FROM ProductSales ps LEFT JOIN FETCH ps.productItemSales ORDER BY ps.id DESC")
        // .firstResult();
        // // Instant createdInstant = lastProductSales.createdTimestamp.atZone(ZoneId.systemDefault()).toInstant();
        // // List<Order> orders = (lastProductSales != null)
        // //     ? Order.findBetweenAfter(start, end, createdInstant)
        // //     : Order.findBetween(start, end);
        // Instant createdInstant;
        // if (lastProductSales != null && lastProductSales.createdTimestamp != null) {
        //     createdInstant = lastProductSales.createdTimestamp.atZone(ZoneId.systemDefault()).toInstant();
        // } else {
        //     createdInstant = Instant.EPOCH; // または start など適切な初期値
        // }
        ProductSales lastProductSales = ProductSales.find(
        "SELECT ps FROM ProductSales ps LEFT JOIN FETCH ps.productItemSales ORDER BY ps.id DESC")
            .firstResult();

        List<Order> orders;
        if (lastProductSales != null && lastProductSales.createdTimestamp != null) {
            Instant createdInstant = lastProductSales.createdTimestamp.atZone(ZoneId.systemDefault()).toInstant();
            orders = Optional.ofNullable(Order.findBetweenAfter(start, end, createdInstant))
                .orElse(Collections.emptyList());
        } else {
            orders = Optional.ofNullable(Order.findBetween(start, end))
                .orElse(Collections.emptyList());
        }

        List<Instant> dateRange = getDatesBetween(start, end);
        List<ProductSales> productSalesList = new ArrayList<>();
    
        for (Item item : Item.values()) {
            // 検索または新規作成
            ProductSales productSales = ProductSales.findByItem(item);
            if (productSales == null) {
                productSales = new ProductSales(item);
                productSales.productItemSales = new ArrayList<>();
            }
    
            // アイテムを含むオーダーだけを抽出
            List<Order> ordersWithProduct = orders.stream()
                .filter(order -> order.getLineItems().stream()
                    .anyMatch(lineItem -> lineItem.getItem().equals(item)))
                .collect(Collectors.toList());

    
            for (Instant date : dateRange) {
                Instant day = date.truncatedTo(ChronoUnit.DAYS);
    
                List<LineItem> lineItemsForDay = ordersWithProduct.stream()
                    .filter(order -> order.getOrderPlacedTimestamp().truncatedTo(ChronoUnit.DAYS).equals(day))
                    .flatMap(order -> order.getLineItems().stream()
                        .filter(lineItem -> lineItem.getItem().equals(item)))
                    .collect(Collectors.toList());
    
                long soldItems = lineItemsForDay.size();
                if (soldItems == 0) continue;
    
                // 既存のレコードがある場合は更新、なければ作成
                ProductItemSales itemSales = ProductItemSales.find("item = :item AND salesdate = :salesdate",
                        Parameters.with("item", item).and("salesdate", day))
                    .firstResult();
    
                if (itemSales != null) {
                    BigDecimal price = item.getPrice();
                    BigDecimal salesTotal = itemSales.salesTotal; // または getSalesTotal()
                    if (price != null && salesTotal != null) {
                        itemSales.revenue = price.multiply(salesTotal);
                    } else {
                        itemSales.revenue = BigDecimal.ZERO; // または適切なデフォルト値
                    }
                } else {
                    ProductItemSales newItemSales = new ProductItemSales(
                        item,
                        BigDecimal.valueOf(soldItems),
                        item.getPrice().multiply(BigDecimal.valueOf(soldItems)),
                        day
                    );
                    newItemSales.productSales = productSales;
                    productSales.productItemSales.add(newItemSales);
                    newItemSales.persist();
                }
            }
            
            productSales.createdTimestamp = functionStart;
            productSales.persist();
            productSalesList.add(productSales);
        }
    
        Instant functionEnd = Instant.now();
        productSalesList.sort(Comparator.comparing(ProductSales::getItem));
        return productSalesList;
    }

    /*
    query itemSalesTotalsByDate {
        itemSalesTotalsByDate(startDate: "2025-01-01", endDate: "2025-12-31") {
            item
            revenue
            salesTotal
        }
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
        return sales;
    }

    /*
    query {
        storeServerSales {
            server
            store
            itemSales {
            item
            salesTotal
            price
            }
        }
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
                   Map<Item, ItemSales> items = new HashMap<>();
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
        return storeServerSalesList;
    }


    /*
    query {
        storeServerSalesByDate(startDate: "2025-01-01", endDate: "2025-12-31") {
            server
            store
            sales {
            item
            salesTotal
            revenue
            }
        }
    }
     */
    @Query
    public List<StoreServerSales> getStoreServerSalesByDate(String startDate, String endDate) {

        Instant start = Instant.parse(startDate + "T00:00:00Z");
        Instant end = Instant.parse(endDate + "T00:00:00Z").plus(1, ChronoUnit.DAYS);
        List<StoreServerSales> storeServerSalesList = new ArrayList<>();

        for (Store location : Store.values()) {
            Map<String, Map<Item, ItemSales>> servers = new HashMap<>();

            List<Order> orders = Order.findBetweenByLocation(location.name(), start, end);

            for (Order order : orders) {
                for (LineItem lineItem : order.getLineItems()) {
                    if (lineItem.getPreparedBy() == null || lineItem.getItem() == null) {
                        continue; // skip null entries
                    }

                    String server = lineItem.getPreparedBy();
                    Item item = lineItem.getItem();
                    BigDecimal price = lineItem.getPrice();

                    servers.putIfAbsent(server, new HashMap<>());
                    Map<Item, ItemSales> itemMap = servers.get(server);

                    itemMap.compute(item, (k, v) -> {
                        if (v == null) {
                            return new ItemSales(item, 1, price.doubleValue());
                        } else {
                            v.salesTotal += 1;
                            v.revenue += price.doubleValue();
                            return v;
                        }
                    });
                }
            }

            // MapからStoreServerSalesリストを構築
            for (Map.Entry<String, Map<Item, ItemSales>> serverEntry : servers.entrySet()) {
                StoreServerSales serverSales = new StoreServerSales();
                serverSales.store = location.name();
                serverSales.server = serverEntry.getKey();
                serverSales.itemSales = new ArrayList<>(serverEntry.getValue().values());
                storeServerSalesList.add(serverSales);
            }
        }

        return storeServerSalesList;
    }

    @Transactional
    @Query
    public int getAverageOrderUpTime(String startDate, String endDate){
        Instant now = Instant.now();
        Instant start = Instant.parse(startDate + "T00:00:00Z");
        Instant end = Instant.parse(endDate + "T00:00:00Z").plus(1, ChronoUnit.DAYS);
        AverageOrderUpTime averageOrderUpTime = AverageOrderUpTime.find("order by calculatedAt desc").firstResult();

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
                averageOrderUpTime.averageTime = Math.min(300, averageTime);
                //averageOrderUpTime.orderCount = (int) orders.stream().count();
                averageOrderUpTime.calculatedAt = now;
                averageOrderUpTime.persist();
            }else{
                int oldTotalTime = averageOrderUpTime.averageTime * averageOrderUpTime.orderCount;
                averageOrderUpTime.averageTime = Math.min(300,
                    (int) ((totalTime + oldTotalTime) / (averageOrderUpTime.orderCount + orders.size()))
                );
                //averageOrderUpTime.orderCount = averageOrderUpTime.orderCount + (int) orders.stream().count();
                averageOrderUpTime.calculatedAt = now;
                averageOrderUpTime.persist();
        }        
            //Instant functionEnd = Instant.now();
            return averageOrderUpTime.averageTime;
        }
    }

    public static List<Instant> getDatesBetween(Instant startDate, Instant endDate) {
        long numOfDaysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        return IntStream.range(0, (int) numOfDaysBetween)
            .mapToObj(i -> startDate.plus(i, ChronoUnit.DAYS))
            .collect(Collectors.toList());
    }
}