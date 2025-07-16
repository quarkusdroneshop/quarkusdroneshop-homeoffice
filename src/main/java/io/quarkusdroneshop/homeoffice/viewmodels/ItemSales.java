package io.quarkusdroneshop.homeoffice.viewmodels;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkusdroneshop.homeoffice.domain.Item;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.OrderRecord;
import io.quarkusdroneshop.homeoffice.domain.Order;

import javax.validation.constraints.NotNull;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Entity
@Table(name="itemsales")
public class ItemSales extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "store_server_sales_id")
    private StoreServerSales storeServerSales;

    @NotNull
    @Enumerated(EnumType.STRING)
    public Item item;

    @NotNull
    public long salesTotal;

    @NotNull
    public double price;

    @NotNull
    public double revenue;

    public Instant date;

    public ItemSales() {}

    public ItemSales(Item item, long salesTotal, double revenue){
        this.item = item;
        this.salesTotal = salesTotal;
        this.revenue = revenue;
    }

    public ItemSales(Item item, long salesTotal, double revenue, Instant date, double price) {
        this.item = item;
        this.salesTotal = salesTotal;
        this.revenue = revenue;
        this.date = Instant.now();
        this.price = price;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setSalesTotal(long salesTotal) {
        this.salesTotal = salesTotal;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public StoreServerSales getStoreServerSales() {
        return this.storeServerSales;
    }

    public void setStoreServerSales(StoreServerSales storeServerSales) {
        this.storeServerSales = storeServerSales;
    }

}
