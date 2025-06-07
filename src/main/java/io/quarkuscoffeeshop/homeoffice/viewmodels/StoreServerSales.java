package io.quarkuscoffeeshop.homeoffice.viewmodels;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.OrderRecord;

import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name="StoreServerSales")
public class StoreServerSales extends PanacheEntity {

    public String store;
    public String server;

    @OneToMany(
        targetEntity = ItemSales.class,
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    public List<ItemSales> sales = new ArrayList<>();

    public static void persist(OrderRecord orderRecord) {

        StoreServerSales storeServerSales = new StoreServerSales();
        storeServerSales.store = "TOKYO";
        storeServerSales.server = "OpenShift";

        // ItemSales を作成
        if (orderRecord.baristaLineItems() != null) {
            for (var line : orderRecord.baristaLineItems()) {
                ItemSales itemSales = new ItemSales(
                    line.item(),
                    1L,
                    java.math.BigDecimal.valueOf(3.00),
                    orderRecord.timestamp()
                );
                storeServerSales.sales.add(itemSales);
            }
        }

        if (orderRecord.kitchenLineItems() != null) {
            for (var line : orderRecord.kitchenLineItems()) {
                ItemSales itemSales = new ItemSales(
                    line.item(),
                    1L,
                    java.math.BigDecimal.valueOf(3.50),
                    orderRecord.timestamp()
                );
                storeServerSales.sales.add(itemSales);
            }
        }
    storeServerSales.persist();
    }
}