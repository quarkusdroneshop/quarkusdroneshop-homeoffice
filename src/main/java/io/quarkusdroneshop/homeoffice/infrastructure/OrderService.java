package io.quarkusdroneshop.homeoffice.infrastructure;

import io.quarkusdroneshop.homeoffice.domain.Item;
import io.quarkusdroneshop.homeoffice.domain.LineItem;
import io.quarkusdroneshop.homeoffice.domain.Order;
import io.quarkusdroneshop.homeoffice.domain.OrderSource;
import io.quarkusdroneshop.homeoffice.domain.view.LineItemSalesReport;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.LineItemRecord;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.OrderRecord;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.StoreLocation;
import io.quarkusdroneshop.homeoffice.viewmodels.ItemSales;
import io.quarkusdroneshop.homeoffice.viewmodels.ProductItemSales;
import io.quarkusdroneshop.homeoffice.viewmodels.AverageOrderUpTime;
import io.quarkusdroneshop.homeoffice.viewmodels.ProductSales;
import io.quarkusdroneshop.homeoffice.viewmodels.StoreServerSales;

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
        String orderId = orderRecord.orderId();
        Order order = Order.find("cast(orderid as text) = ?1", orderId).firstResult();
        boolean existenceOrder = (order != null);

        if (existenceOrder == true) {
            order.orderCompletedTimestamp = Instant.now();
            order.persist();

            AverageOrderUpTime updated = AverageOrderUpTime.fromOrderRecord(order);
            if (updated != null) {
                updated.persist();
            }

         } else {
            order = convertOrderRecordToOrder(orderRecord);
            order.persist();

            List<ProductItemSales> salesList = convertOrderRecordToProductItemSales(orderRecord);

            for (ProductItemSales sales : salesList) {
                ProductSales productSales = ProductSales.findByItem(sales.item);
            
                if (productSales == null) {
                    productSales = new ProductSales(sales.item);
                }
            
                ProductItemSales itemSales = new ProductItemSales(
                    sales.item,
                    sales.salesTotal,
                    sales.revenue,
                    Instant.now()
                );
                
                productSales.addProductItemSale(itemSales);
                productSales.persist();

                StoreServerSales.persist(orderRecord);
            }
        }
    }

    protected Order convertOrderRecordToOrder(final OrderRecord orderRecord) {
        List<LineItem> lineItems = new ArrayList<>();
    
        if (orderRecord.getQdca10LineItems() != null) {
            for (LineItemRecord record : orderRecord.getQdca10LineItems()) {
                BigDecimal price = BigDecimal.valueOf(record.getPrice());
                //BigDecimal price = BigDecimal.valueOf(3.00); // 固定価格
                Item item = record.getItem();
                // 必要なフィールドを追加でセット
                //itemSales.setPreparedBy(sales.preparedBy); // ← preparedBy をセット
                lineItems.add(new LineItem(item, price, "QDCA10"));
            }
        }
        
        if (orderRecord.getQdca10proLineItems() != null) {
            for (LineItemRecord record : orderRecord.getQdca10proLineItems()) {
                BigDecimal price = BigDecimal.valueOf(record.getPrice());
                //BigDecimal price = BigDecimal.valueOf(3.50); // 固定価格
                Item item = record.getItem();
                lineItems.add(new LineItem(item, price, "QDCA10Pro"));
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
    
        if (orderRecord.getQdca10LineItems() != null) {
            for (LineItemRecord record : orderRecord.getQdca10LineItems()) {
                Item item = record.getItem();
                //BigDecimal price = BigDecimal.valueOf(3.00); // QDCA10用価格
                BigDecimal price = BigDecimal.valueOf(record.getPrice());
                itemCounts.merge(item, 1L, Long::sum);
                itemRevenue.merge(item, price, BigDecimal::add);
            }
        }
        
        if (orderRecord.getQdca10proLineItems() != null) {
            for (LineItemRecord record : orderRecord.getQdca10proLineItems()) {
                Item item = record.getItem();
                //BigDecimal price = BigDecimal.valueOf(3.50); // QDCA10Pro用価格
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

