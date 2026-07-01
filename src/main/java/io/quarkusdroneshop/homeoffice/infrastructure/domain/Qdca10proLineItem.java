package io.quarkusdroneshop.homeoffice.infrastructure.domain;

import jakarta.persistence.GeneratedValue;
import io.quarkusdroneshop.homeoffice.domain.Item;
import io.quarkusdroneshop.homeoffice.viewmodels.StoreServerSales;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import java.math.BigDecimal;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Objects;

@Entity
public class Qdca10proLineItem extends PanacheEntityBase {
    @Id
    @GeneratedValue
    public Long id;

    public Item item;
    public Double price;
    public String name;
        public Item getItem() {
        return item;
    }

    @ManyToOne
    @JoinColumn(name = "store_server_sales_id")
    private StoreServerSales storeServerSales;

    public void setItem(Item item) {
        this.item = item;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public StoreServerSales getStoreServerSales() {
        return storeServerSales;
    }

    public void setStoreServerSales(StoreServerSales storeServerSales) {
        this.storeServerSales = storeServerSales;
    }
    
}