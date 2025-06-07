package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkuscoffeeshop.homeoffice.domain.Item;
import io.quarkuscoffeeshop.homeoffice.domain.LineItem;
import io.quarkuscoffeeshop.homeoffice.domain.Order;
import io.quarkuscoffeeshop.homeoffice.domain.view.LineItemSalesReport;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.OrderRecord;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.StoreLocation;
import io.quarkuscoffeeshop.homeoffice.viewmodels.ItemSales;
import io.quarkuscoffeeshop.homeoffice.viewmodels.ProductItemSales;
import io.quarkuscoffeeshop.homeoffice.viewmodels.AverageOrderUpTime;
import io.quarkuscoffeeshop.homeoffice.viewmodels.ProductSales;
import io.quarkuscoffeeshop.homeoffice.viewmodels.StoreServerSales;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrderService {

    public List<Order> allOrders() {
        return Order.listAll();
    }

    public List<Order> allOrdersByLocation(final StoreLocation storeLocation) {
        return Order.find("location = :location", new HashMap<String, Object>(){{ put("location", storeLocation); }}).list();
    }

    public List<LineItem> getLineItemSales() {
        List<LineItem> lineItemList = LineItem.listAll();
        Map<Item, List<LineItem>> lineItemMap =
                lineItemList.stream().collect(Collectors.groupingBy(LineItem::getItem));
        return null;
    }

    @Transactional
    public void process(OrderRecord orderRecord) {
        Order order = convertOrderRecordToOrder(orderRecord);
        order.persist();
    
        //lineitems
        for (LineItem lineItem : order.getLineItems()) {
            lineItem.persist();
        }
    
        //itemsales
        for (LineItem lineItem : order.getLineItems()) {
            lineItem.setOrder(order);  // 外部キー紐付け
            lineItem.persist();
    
            // LineItemごとにItemSalesを作成・保存
            ItemSales itemSales = new ItemSales(
                lineItem.getItem(),
                1L,  // 1点の売上
                lineItem.getPrice(),
                order.getCreatedAt()  // または Instant.now()
            );
            itemSales.persist();
        }
        
        // productsales / productitemsales
        List<ProductItemSales> salesList = convertOrderRecordToProductItemSales(orderRecord);
        for (ProductItemSales sales : salesList) {
            ProductSales productSales = ProductSales.findByItem(sales.item);
            productSales.productItemSales.add(sales);
            productSales.persist();
            sales.persist();
        }

        // averageorderuptime
        AverageOrderUpTime.updateFromOrderRecord(orderRecord);
    
        // 7. storeserversales
        StoreServerSales.persist(orderRecord);
    
        // 8. storeserversales_itemsales
        //StoreServerSalesItemSales.persist(orderRecord);
    }


    protected Order convertOrderRecordToOrder(final OrderRecord orderRecord) {

        List<LineItem> lineItems = new ArrayList<>();
        if (orderRecord.baristaLineItems() != null) {
            orderRecord.baristaLineItems().forEach(l -> {
                lineItems.add(new LineItem(l.item(), BigDecimal.valueOf(3.00), l.name()));
            });
        }
        if ((orderRecord.kitchenLineItems() != null)) {
            orderRecord.kitchenLineItems().forEach(k -> {
                lineItems.add(new LineItem(k.item(), BigDecimal.valueOf(3.50), k.name()));
            });
        }
        return new Order(
                orderRecord.orderId(),
                lineItems,
                orderRecord.orderSource(),
                "TOKYO",
                orderRecord.loyaltyMemberId() == null ? null : orderRecord.loyaltyMemberId(),
                orderRecord.timestamp(),
                Instant.now()
        );
    }

    protected List<ProductItemSales> convertOrderRecordToProductItemSales(final OrderRecord orderRecord) {

        Map<Item, Long> itemCounts = new HashMap<>();
        Map<Item, BigDecimal> itemRevenue = new HashMap<>();
    
        if (orderRecord.baristaLineItems() != null) {
            for (var l : orderRecord.baristaLineItems()) {
                itemCounts.merge(l.item(), 1L, Long::sum);
                itemRevenue.merge(l.item(), BigDecimal.valueOf(3.00), BigDecimal::add);
            }
        }
    
        if (orderRecord.kitchenLineItems() != null) {
            for (var k : orderRecord.kitchenLineItems()) {
                itemCounts.merge(k.item(), 1L, Long::sum);
                itemRevenue.merge(k.item(), BigDecimal.valueOf(3.50), BigDecimal::add);
            }
        }
    
        Instant now = Instant.now();
        List<ProductItemSales> salesList = new ArrayList<>();
        for (Item item : itemCounts.keySet()) {
            salesList.add(new ProductItemSales(
                item,
                itemCounts.get(item),
                itemRevenue.get(item),
                now
            ));
        }
    
        return salesList;
    }

}
