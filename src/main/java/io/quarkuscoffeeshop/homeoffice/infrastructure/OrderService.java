package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkuscoffeeshop.homeoffice.domain.Item;
import io.quarkuscoffeeshop.homeoffice.domain.LineItem;
import io.quarkuscoffeeshop.homeoffice.domain.Order;
import io.quarkuscoffeeshop.homeoffice.domain.OrderSource;
import io.quarkuscoffeeshop.homeoffice.domain.view.LineItemSalesReport;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.LineItemRecord;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.OrderRecord;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.StoreLocation;
import io.quarkuscoffeeshop.homeoffice.viewmodels.ItemSales;
import io.quarkuscoffeeshop.homeoffice.viewmodels.ProductItemSales;
import io.quarkuscoffeeshop.homeoffice.viewmodels.AverageOrderUpTime;
import io.quarkuscoffeeshop.homeoffice.viewmodels.ProductSales;
import io.quarkuscoffeeshop.homeoffice.viewmodels.StoreServerSales;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    public List<Order> allOrders() {
        return Order.listAll();
    }

    public List<Order> allOrdersByLocation(final StoreLocation storeLocation) {
        return Order.find("location = :location", new HashMap<String, Object>(){{ put("location", storeLocation); }}).list();
    }

    @Transactional
    public void process(OrderRecord orderRecord) {
        // --- 既存コード ---
        // どちらかのIDにマッチするOrderを検索
        String orderId = orderRecord.orderId(); // 外部システムが使うID
        Order order = Order.find("cast(orderid as text) = ?1", orderId).firstResult();
        boolean existenceOrder = (order != null);

        if (existenceOrder == true) {
            order.orderCompletedTimestamp = Instant.now();
            order.persist();  // ここで UPDATE が発行される

            // 追加: AverageOrderUpTime の更新
            AverageOrderUpTime updated = AverageOrderUpTime.fromOrderRecord(order);
            if (updated != null) {
                updated.persist();
            }

         } else {
            order = convertOrderRecordToOrder(orderRecord);
            order.persist();

            // ここで salesList を取得
            List<ProductItemSales> salesList = convertOrderRecordToProductItemSales(orderRecord);

            //
            for (ProductItemSales sales : salesList) {
                ProductSales productSales = ProductSales.findByItem(sales.item);
            
                if (productSales == null) {
                    productSales = new ProductSales(sales.item);
                }
            
                // sales の情報を使って新しい itemSales を作成
                ProductItemSales itemSales = new ProductItemSales(
                    sales.item,
                    sales.salesTotal,
                    sales.revenue,
                    Instant.now()
                );
                
                productSales.addProductItemSale(itemSales);
                productSales.persist();

                //
                StoreServerSales.persist(orderRecord);
            }
        }
    }

    protected Order convertOrderRecordToOrder(final OrderRecord orderRecord) {
        List<LineItem> lineItems = new ArrayList<>();
    
        if (orderRecord.getBaristaLineItems() != null) {
            for (LineItemRecord record : orderRecord.getBaristaLineItems()) {
                BigDecimal price = BigDecimal.valueOf(record.getPrice());
                //BigDecimal price = BigDecimal.valueOf(3.00); // 固定価格
                Item item = record.getItem();
                // 必要なフィールドを追加でセット
                //itemSales.setPreparedBy(sales.preparedBy); // ← preparedBy をセット
                lineItems.add(new LineItem(item, price, "barista"));
            }
        }
        
        if (orderRecord.getKitchenLineItems() != null) {
            for (LineItemRecord record : orderRecord.getKitchenLineItems()) {
                BigDecimal price = BigDecimal.valueOf(record.getPrice());
                //BigDecimal price = BigDecimal.valueOf(3.50); // 固定価格
                Item item = record.getItem();
                lineItems.add(new LineItem(item, price, "kitchen"));
            }
        }
        
        return new Order(
            UUID.randomUUID().toString(),
            lineItems,
            orderRecord.orderSource(),
            orderRecord.location() != null ? orderRecord.location() : "TOKYO",
            orderRecord.externalOrderId(),
            orderRecord.customerLoyaltyId(),
            Instant.now(),
            Instant.now()
        );
    }

    protected List<ProductItemSales> convertOrderRecordToProductItemSales(final OrderRecord orderRecord) {

        Map<Item, Long> itemCounts = new HashMap<>();
        Map<Item, BigDecimal> itemRevenue = new HashMap<>();
    
        if (orderRecord.getBaristaLineItems() != null) {
            for (LineItemRecord record : orderRecord.getBaristaLineItems()) {
                Item item = record.getItem();
                //BigDecimal price = BigDecimal.valueOf(3.00); // barista用価格
                BigDecimal price = BigDecimal.valueOf(record.getPrice());
                itemCounts.merge(item, 1L, Long::sum);
                itemRevenue.merge(item, price, BigDecimal::add);
            }
        }
        
        if (orderRecord.getKitchenLineItems() != null) {
            for (LineItemRecord record : orderRecord.getKitchenLineItems()) {
                Item item = record.getItem();
                //BigDecimal price = BigDecimal.valueOf(3.50); // kitchen用価格
                BigDecimal price = BigDecimal.valueOf(record.getPrice());
                itemCounts.merge(item, 1L, Long::sum);
                itemRevenue.merge(item, price, BigDecimal::add);
            }
        }
        
        Instant now = Instant.now();
        List<ProductItemSales> salesList = new ArrayList<>();
        for (Item item : itemCounts.keySet()) {
            BigDecimal revenue = itemRevenue.get(item);
            BigDecimal salesTotal = itemRevenue.get(item);
            ProductItemSales sales = new ProductItemSales(item, salesTotal, revenue, now);
            sales.setSalesTotal(revenue);
            sales.setSalesTotal(salesTotal);
            salesList.add(sales);
        }
        
        return salesList;

    }
}

