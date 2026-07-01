package io.quarkusdroneshop.homeoffice.infrastructure;

import io.quarkus.panache.common.Parameters;
import io.quarkusdroneshop.homeoffice.domain.*;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.Qdca10LineItem;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.Qdca10proLineItem;
import io.quarkusdroneshop.homeoffice.viewmodels.*;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@GraphQLApi
//@ApplicationScoped
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
                ProductItemSales itemSales =
                    ProductItemSales.find(
                        "item = :item AND saleDate = :saleDate",
                        Parameters.with("item", item)
                                .and("saleDate", day))
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
            itemSales {
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
        logger.info("### averageOrderUpTime resolver called ###");
        
        Instant now = Instant.now();
        
        // JST → UTC に変換
        ZonedDateTime startJst = LocalDate.parse(startDate).atStartOfDay(ZoneId.of("Asia/Tokyo"));
        Instant startUtc = startJst.toInstant();

        ZonedDateTime endJst = LocalDate.parse(endDate).plusDays(1).atStartOfDay(ZoneId.of("Asia/Tokyo"));
        Instant endUtc = endJst.toInstant();

        // 注文取得
        List<Order> orders = Order.findBetween(startUtc, endUtc);

        double totalMillis = 0;
        int validCount = 0;

        for (Order order : orders) {
            Instant placed = order.getOrderPlacedTimestamp();
            Instant completed = order.getOrderCompletedTimestamp();
            if (placed == null || completed == null) continue;

            // Duration をミリ秒で計算（1ms 未満は無効データとして除外）
            double millis = Duration.between(placed, completed).toNanos() / 1_000_000.0;
            if (millis < 1.0) continue;

            totalMillis += millis;
            validCount++;
        }

        if (validCount == 0) {
            logger.warn("No valid orders -> return 0.0");
            return 0;
        }

        double avgMillis = totalMillis / validCount;
        avgMillis = Math.min(300_000.0, avgMillis);

        // デモ用に最新レコード更新（DB保存は整数ミリ秒）
        AverageOrderUpTime latest = AverageOrderUpTime.find("order by calculatedAt desc").firstResult();
        if (latest == null) {
            latest = new AverageOrderUpTime();
            latest.averageTime = (int) Math.round(avgMillis);
            latest.orderCount = validCount;
        } else {
            double oldTotal = (double) latest.averageTime * latest.orderCount;
            int newCount = latest.orderCount + validCount;
            latest.averageTime = (int) Math.min(300_000.0, (oldTotal + totalMillis) / newCount);
            latest.orderCount = newCount;
        }

        latest.calculatedAt = now;
        latest.persist();

        // ミリ秒のまま返す
        return (int) avgMillis;
    }

    public static List<Instant> getDatesBetween(Instant startDate, Instant endDate) {
        long numOfDaysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        return IntStream.range(0, (int) numOfDaysBetween)
            .mapToObj(i -> startDate.plus(i, ChronoUnit.DAYS))
            .collect(Collectors.toList());
    }

    /**
     * 各マイクロサービスの /q/health を確認してステータスを返す。
     * サービス URL は環境変数 HEALTH_URL_<NAME> で設定する。
     */
    @Query
    public List<io.quarkusdroneshop.homeoffice.viewmodels.ServiceHealth> serviceHealthChecks() {
        Map<String, String> services = new LinkedHashMap<>();
        services.put("Web",        System.getenv().getOrDefault("HEALTH_URL_WEB",       ""));
        services.put("Counter",    System.getenv().getOrDefault("HEALTH_URL_COUNTER",   ""));
        services.put("QDCA10",     System.getenv().getOrDefault("HEALTH_URL_QDCA10",    ""));
        services.put("QDCA10Pro",  System.getenv().getOrDefault("HEALTH_URL_QDCA10PRO", ""));
        services.put("Inventory",  System.getenv().getOrDefault("HEALTH_URL_INVENTORY", ""));
        services.put("Homeoffice", System.getenv().getOrDefault("HEALTH_URL_HOMEOFFICE", "http://localhost:8080/q/health"));

        HttpClient http = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(5))
            .build();

        List<io.quarkusdroneshop.homeoffice.viewmodels.ServiceHealth> results = new ArrayList<>();
        for (Map.Entry<String, String> entry : services.entrySet()) {
            String name = entry.getKey();
            String url  = entry.getValue();
            if (url == null || url.isBlank()) {
                results.add(new io.quarkusdroneshop.homeoffice.viewmodels.ServiceHealth(name, "UNKNOWN", "URL not configured"));
                continue;
            }
            try {
                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .GET()
                    .build();
                HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
                String body = res.body();
                // JSON に "status":"UP" が含まれていれば UP
                String status = (res.statusCode() == 200 && body.contains("\"status\":\"UP\"")) ? "UP" : "DOWN";
                results.add(new io.quarkusdroneshop.homeoffice.viewmodels.ServiceHealth(name, status, "HTTP " + res.statusCode()));
            } catch (Exception e) {
                logger.warn("Health check failed for {}: {}", name, e.getMessage());
                results.add(new io.quarkusdroneshop.homeoffice.viewmodels.ServiceHealth(name, "DOWN", e.getMessage()));
            }
        }
        return results;
    }

    /**
     * 直近 4 時間の注文をライブボード用に返す。
     * status は orderCompletedTimestamp / lineItem.preparedBy から推定する。
     */
    @Query
    public List<io.quarkusdroneshop.homeoffice.viewmodels.LiveOrder> liveOrders() {
        Instant since = Instant.now().minus(4, ChronoUnit.HOURS);
        List<Order> orders = Order.find(
            "orderPlacedTimestamp >= :since ORDER BY orderPlacedTimestamp DESC",
            Parameters.with("since", since)
        ).list();

        List<io.quarkusdroneshop.homeoffice.viewmodels.LiveOrder> result = new ArrayList<>();
        for (Order o : orders) {
            // item: 最初の lineItem の item 名
            String itemName = (o.getLineItems() != null && !o.getLineItems().isEmpty())
                ? o.getLineItems().iterator().next().getItem().name()
                : "UNKNOWN";

            // madeBy: 最初の lineItem の preparedBy
            String madeBy = (o.getLineItems() != null && !o.getLineItems().isEmpty())
                ? o.getLineItems().iterator().next().getPreparedBy()
                : null;

            // status 判定
            String status;
            if (o.getOrderCompletedTimestamp() != null) {
                status = "FULFILLED";
            } else if (madeBy != null && !madeBy.isBlank()) {
                status = "IN_PROGRESS";
            } else {
                status = "IN_QUEUE";
            }

            // name: loyaltyMemberId があれば使用、なければ orderId 末尾 8 文字
            String name = (o.getCustomerLoyaltyId() != null && !o.getCustomerLoyaltyId().isBlank())
                ? o.getCustomerLoyaltyId()
                : o.getOrderId().substring(Math.max(0, o.getOrderId().length() - 8));

            String createdAt = o.getOrderPlacedTimestamp() != null
                ? o.getOrderPlacedTimestamp().toString() : null;
            String updatedAt = o.getOrderCompletedTimestamp() != null
                ? o.getOrderCompletedTimestamp().toString() : null;

            result.add(new io.quarkusdroneshop.homeoffice.viewmodels.LiveOrder(
                o.getOrderId(), name, itemName, status, madeBy, o.getLocation(), createdAt, updatedAt
            ));
        }
        return result;
    }

    /**
     * 30分以上経過しても未完了の注文を「要対応注文」として返す。
     * orderCompletedTimestamp が null かつ orderPlacedTimestamp が threshold より古いものを対象とする。
     */
    @Query
    public List<FailedOrder> failedOrders() {
        Instant threshold = Instant.now().minus(30, ChronoUnit.MINUTES);
        List<Order> stuckOrders = Order.find(
            "orderPlacedTimestamp < :threshold AND orderCompletedTimestamp IS NULL ORDER BY orderPlacedTimestamp ASC",
            Parameters.with("threshold", threshold)
        ).list();

        List<FailedOrder> result = new ArrayList<>();
        for (Order o : stuckOrders) {
            String itemName = (o.getLineItems() != null && !o.getLineItems().isEmpty())
                ? o.getLineItems().iterator().next().getItem().name()
                : "UNKNOWN";

            String name = (o.getCustomerLoyaltyId() != null && !o.getCustomerLoyaltyId().isBlank())
                ? o.getCustomerLoyaltyId()
                : o.getOrderId().substring(Math.max(0, o.getOrderId().length() - 8));

            String failedAt = o.getOrderPlacedTimestamp() != null
                ? o.getOrderPlacedTimestamp().toString() : Instant.now().toString();

            // madeBy がある = 処理中だがタイムアウト、ない = キューで滞留
            boolean hasWorker = o.getLineItems() != null && !o.getLineItems().isEmpty()
                && o.getLineItems().iterator().next().getPreparedBy() != null
                && !o.getLineItems().iterator().next().getPreparedBy().isBlank();
            String failureReason = hasWorker ? "PROCESSING_TIMEOUT" : "QUEUE_TIMEOUT";

            result.add(new FailedOrder(o.getOrderId(), name, itemName, failureReason, failedAt, 0));
        }
        return result;
    }

    /**
     * 要対応注文のリトライ: 対象注文を完了済みとしてマークし、画面上から除去する。
     * 実際の再処理は Kafka 経由で行うべきだが、デモ用として DB 上でのみ操作する。
     */
    @Mutation
    @Transactional
    public RetryResult retryOrder(String orderId) {
        Order order = Order.find("orderId", orderId).firstResult();
        if (order == null) {
            return new RetryResult(false, "注文 " + orderId + " が見つかりません");
        }
        // デモ: 完了タイムスタンプを付与して「要対応」リストから除外する
        order.orderCompletedTimestamp = Instant.now();
        order.persist();
        return new RetryResult(true, "注文 " + orderId + " をリトライキューに送信しました");
    }

    public static class RetryResult {
        public boolean success;
        public String message;
        public RetryResult() {}
        public RetryResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        public boolean isSuccess() { return success; }
        public String getMessage()  { return message; }
    }

    /**
     * 全注文データをリセットする。
     * FK 制約に従い以下の順で削除:
     *   lineItems → itemsales / qdca10lineitems / qdca10prolineitems
     *   → orders → storeserversales → productitemsales → productsales → averageorderuptime
     */
    @Mutation
    @Transactional
    public ResetResult resetData() {
        logger.warn("### resetData mutation called — deleting all order data ###");
        try {
            long lineItems       = LineItem.deleteAll();
            long itemSales       = ItemSales.deleteAll();
            long qdca10Items     = Qdca10LineItem.deleteAll();
            long qdca10proItems  = Qdca10proLineItem.deleteAll();
            long orders          = Order.deleteAll();
            long storeServerSales = StoreServerSales.deleteAll();
            long productItemSales = ProductItemSales.deleteAll();
            long productSales    = ProductSales.deleteAll();
            long avgOrderUpTime  = AverageOrderUpTime.deleteAll();

            String summary = String.format(
                "orders=%d, lineItems=%d, itemSales=%d, storeServerSales=%d, " +
                "productSales=%d, productItemSales=%d, avgOrderUpTime=%d",
                orders, lineItems, itemSales, storeServerSales,
                productSales, productItemSales, avgOrderUpTime);
            logger.info("resetData complete: {}", summary);
            return new ResetResult(true, "データをリセットしました。" + summary);
        } catch (Exception e) {
            logger.error("resetData failed", e);
            return new ResetResult(false, "リセット失敗: " + e.getMessage());
        }
    }

    public static class ResetResult {
        public boolean success;
        public String message;
        public ResetResult() {}
        public ResetResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        public boolean isSuccess() { return success; }
        public String getMessage()  { return message; }
    }
}