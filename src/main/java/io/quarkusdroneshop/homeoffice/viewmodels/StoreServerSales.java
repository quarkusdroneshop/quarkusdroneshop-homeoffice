package io.quarkusdroneshop.homeoffice.viewmodels;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkusdroneshop.homeoffice.domain.Item;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.LineItemRecord;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.OrderRecord;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.QDCA10LineItem;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.QDCA10ProLineItem;


import javax.persistence.*;
import java.util.List;
import java.time.Instant;
import java.util.ArrayList;

@Entity
@Table(name = "StoreServerSales")
public class StoreServerSales extends PanacheEntity {

    public String store;
    public String server;

    @OneToMany(mappedBy = "storeServerSales", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<QDCA10LineItem> QDCA10LineItems = new ArrayList<>();

    @OneToMany(mappedBy = "storeServerSales", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<QDCA10ProLineItem> QDCA10ProLineItems = new ArrayList<>();

    @OneToMany(mappedBy = "storeServerSales", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ItemSales> itemSales = new ArrayList<>();

    public static void persist(OrderRecord orderRecord) {
        StoreServerSales storeServerSales = new StoreServerSales();
        storeServerSales.store = "TOKYO";

        if (orderRecord.getQDCA10LineItems() != null) {
            for (LineItemRecord lineItem : orderRecord.getQDCA10LineItems()) {
                storeServerSales.QDCA10LineItems.add(toQDCA10LineItem(lineItem));
                storeServerSales.server = "QDCA10";
                storeServerSales.itemSales.add(toItemSales(lineItem));
            }
        }
        if (orderRecord.getQDCA10ProLineItems() != null) {
            for (LineItemRecord lineItem : orderRecord.getQDCA10ProLineItems()) {
                storeServerSales.QDCA10ProLineItems.add(toQDCA10ProLineItem(lineItem));
                storeServerSales.server = "QDCA10Pro";
                storeServerSales.itemSales.add(toItemSales(lineItem));
            }
        }

        storeServerSales.persist();
    }

    public static QDCA10LineItem toQDCA10LineItem(LineItemRecord lineItemRecord) {
        QDCA10LineItem item = new QDCA10LineItem();
        item.setItem(lineItemRecord.getItem());
        item.setName(lineItemRecord.getName());
        item.setPrice(lineItemRecord.getPrice());
        return item;
    }

    public static QDCA10ProLineItem toQDCA10ProLineItem(LineItemRecord lineItemRecord) {
        QDCA10ProLineItem item = new QDCA10ProLineItem();
        item.setItem(lineItemRecord.getItem());
        item.setName(lineItemRecord.getName());
        item.setPrice(lineItemRecord.getPrice());
        return item;
    }

    public static ItemSales toItemSales(LineItemRecord lineItemRecord) {
        ItemSales item = new ItemSales();
        item.setItem(lineItemRecord.getItem());
        item.setPrice(lineItemRecord.getPrice());
        item.setSalesTotal(1);
        item.setRevenue(lineItemRecord.getPrice());
        Instant.now();
        return item;
    }
}