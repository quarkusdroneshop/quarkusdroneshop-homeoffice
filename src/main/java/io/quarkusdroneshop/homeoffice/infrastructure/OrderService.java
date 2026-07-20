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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

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
        // 以前は常に null になる externalOrderId で検索していたため既存注文が
        // 見つからず、更新のたびに新しいゴミ注文が作られ続けていた。
        // 本来の一意キーである orderId（counter が発行した本物の注文ID）で検索する。
        Order order = Order.find("orderId", orderId).firstResult();
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
                productSales.persist(); // persist するが、まだ transaction は終わっていない
            }
            
            // ← ループの外で StoreServerSales を persist（この呼び出しの中で itemSales.persist() される）
            StoreServerSales.persist(orderRecord);
        }
    }

    protected Order convertOrderRecordToOrder(final OrderRecord orderRecord) {
        List<LineItem> lineItems = new ArrayList<>();

        if (orderRecord.getQdca10LineItems() != null) {
            for (LineItemRecord record : orderRecord.getQdca10LineItems()) {
                BigDecimal price = BigDecimal.valueOf(record.getPrice());
                Item item = record.getItem();
                lineItems.add(newLineItem(item, price, null, record.getId()));
            }
        }

        if (orderRecord.getQdca10proLineItems() != null) {
            for (LineItemRecord record : orderRecord.getQdca10proLineItems()) {
                BigDecimal price = BigDecimal.valueOf(record.getPrice());
                Item item = record.getItem();
                lineItems.add(newLineItem(item, price, null, record.getId()));
            }
        }

        // 本来の一意キーである counter 発行の orderId をそのまま Order の主キーとして使う。
        // 以前は UUID.randomUUID() を使っていたため、qdca10 からの OrderUp 通知が
        // 二度とこの注文と紐付かなくなっていた。orderId が取れない不正なメッセージの
        // 場合のみ従来どおりランダム生成にフォールバックする。
        String orderId = orderRecord.orderId() != null ? orderRecord.orderId() : UUID.randomUUID().toString();
        return new Order(
            orderId,
            lineItems,
            orderRecord.orderSource(),
            orderRecord.location() != null ? orderRecord.location() : "TOKYO",
            orderRecord.externalOrderId(),
            orderRecord.customerLoyaltyId(),
            Instant.now(),
            null
        );
    }

    /**
     * counter 側で発行された本物の lineItemId を保持しておくことで、
     * 後続の OrderUp（qdca10 / qdca10pro からの完了通知）を正しく突き合わせられるようにする。
     * パース不能または未指定の場合は Hibernate の自動生成に任せる。
     */
    private LineItem newLineItem(Item item, BigDecimal price, String preparedBy, String rawId) {
        LineItem lineItem = new LineItem(item, price, preparedBy);
        if (rawId != null) {
            try {
                lineItem.id = UUID.fromString(rawId);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("lineItemId '{}' is not a valid UUID, letting Hibernate generate one", rawId);
            }
        }
        return lineItem;
    }

    protected List<ProductItemSales> convertOrderRecordToProductItemSales(final OrderRecord orderRecord) {

        Map<Item, Long> itemCounts = new HashMap<>();
        Map<Item, BigDecimal> itemRevenue = new HashMap<>();
    
        if (orderRecord.getQdca10LineItems() != null) {
            for (LineItemRecord record : orderRecord.getQdca10LineItems()) {
                Item item = record.getItem();
                BigDecimal price = BigDecimal.valueOf(record.getPrice());
                itemCounts.merge(item, 1L, Long::sum);
                itemRevenue.merge(item, price, BigDecimal::add);
            }
        }
        
        if (orderRecord.getQdca10proLineItems() != null) {
            for (LineItemRecord record : orderRecord.getQdca10proLineItems()) {
                Item item = record.getItem();
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

