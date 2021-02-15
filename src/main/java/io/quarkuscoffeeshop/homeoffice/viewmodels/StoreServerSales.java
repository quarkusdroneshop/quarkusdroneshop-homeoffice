package io.quarkuscoffeeshop.homeoffice.viewmodels;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="StoreServerSales")
public class StoreServerSales extends PanacheEntity {
    public String store;
    public String server;

    @OneToMany(targetEntity=ItemSales.class,cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    public List<ItemSales> sales;

}
