package io.quarkuscoffeeshop.homeoffice.viewmodels;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkuscoffeeshop.homeoffice.domain.Item;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.OrderRecord;
import io.quarkuscoffeeshop.homeoffice.domain.Order;

import javax.validation.constraints.NotNull;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Entity
@Table(name="ItemSales")
public class ItemSales extends PanacheEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    public Item item;

    @NotNull
    public long salesTotal;

    @NotNull
    public Instant date;

    @NotNull
    public BigDecimal revenue;

    public ItemSales() {}

    public ItemSales(Item item, long salesTotal, BigDecimal revenue){
        this.item = item;
        this.salesTotal = salesTotal;
        this.revenue = revenue;
    }

    public ItemSales(Item item, long salesTotal, BigDecimal revenue, Instant date){
        this.item = item;
        this.salesTotal = salesTotal;
        this.date = date;
        this.revenue = revenue;
    }

    public static void persist(OrderRecord orderRecord, Order order) {
        Item item = order.getItem();                  // 商品
        long salesTotal = 1L;                         // 一件＝1売上（固定）
        BigDecimal revenue = item.getPrice();           // 金額
        Instant date = order.getCreatedAt();          // 日付
    
        ItemSales itemSales = new ItemSales(item, salesTotal, revenue, date);
        itemSales.persist();
    }
}
