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
    public String preparedBy;

    @NotNull
    public long salesTotal;

    @NotNull
    public double price;

    @NotNull
    public Instant date;

    @NotNull
    public double revenue;

    public ItemSales() {}

    public ItemSales(Item item, long salesTotal, double revenue){
        this.item = item;
        this.salesTotal = salesTotal;
        this.revenue = revenue;
    }

    public ItemSales(Item item, long salesTotal, double revenue, Instant date){
        this.item = item;
        this.salesTotal = salesTotal;
        this.date = date;
        this.revenue = revenue;
    }

    public static void persist(OrderRecord orderRecord, Order order) {
        Item item = order.getItem();
        long salesTotal = 1L;
        BigDecimal revenue = item.getPrice();
        Instant date = order.getCreatedAt();
    
        ItemSales itemSales = new ItemSales(item, salesTotal, revenue.doubleValue(), date);
        itemSales.persist();
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void setPreparedBy(String preparedBy) {
        this.preparedBy = preparedBy;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setSalesTotal(int salesTotal) {
        this.salesTotal = salesTotal;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }
}
