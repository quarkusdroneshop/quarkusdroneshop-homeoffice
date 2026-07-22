package io.quarkusdroneshop.homeoffice.infrastructure;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.math.BigDecimal;

/**
 * dataproduct-order-events (Avro) から取り出した ORDER_PLACED の明細1件分。
 * OrderAssemblyAggregator が orderId 単位に集約し、最終的に homeoffice の
 * OrderRecord (注文全体) を組み立てる際の入力単位。
 */
@RegisterForReflection
public class OrderPlacedLineItem {
    public String orderId;
    public String orderSource;
    public String location;
    public String loyaltyMemberId;
    public String itemId;
    public String item;
    public String name;
    public BigDecimal price;
    public String assemblyLine;

    public OrderPlacedLineItem() {}

    public OrderPlacedLineItem(String orderId, String orderSource, String location, String loyaltyMemberId,
                                String itemId, String item, String name, BigDecimal price, String assemblyLine) {
        this.orderId = orderId;
        this.orderSource = orderSource;
        this.location = location;
        this.loyaltyMemberId = loyaltyMemberId;
        this.itemId = itemId;
        this.item = item;
        this.name = name;
        this.price = price;
        this.assemblyLine = assemblyLine;
    }
}
