package io.quarkusdroneshop.homeoffice.viewmodels;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkusdroneshop.homeoffice.domain.Item;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.LineItemRecord;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.OrderRecord;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.Qdca10LineItem;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.Qdca10proLineItem;


import javax.persistence.*;
import javax.transaction.Transactional;

import java.util.List;
import java.time.Instant;
import java.util.ArrayList;

@Entity
@Transactional
@Table(name = "storeserversales")
public class StoreServerSales extends PanacheEntity {

    public String store;
    public String server;

    @OneToMany(mappedBy = "storeServerSales", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Qdca10LineItem> Qdca10LineItems = new ArrayList<>();

    @OneToMany(mappedBy = "storeServerSales", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Qdca10proLineItem> Qdca10proLineItems = new ArrayList<>();

    @OneToMany(mappedBy = "storeServerSales", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ItemSales> itemSales = new ArrayList<>();

    public static void persist(OrderRecord orderRecord) {
        StoreServerSales storeServerSales = new StoreServerSales();
        storeServerSales.store = "TOKYO";

        if (orderRecord.getQdca10LineItems() != null) {
            for (LineItemRecord lineItem : orderRecord.getQdca10LineItems()) {
                storeServerSales.Qdca10LineItems.add(toQdca10LineItem(lineItem, storeServerSales));
                storeServerSales.server = "qdca10";
                storeServerSales.itemSales.add(toItemSales(lineItem,storeServerSales));
            }
        }
        if (orderRecord.getQdca10proLineItems() != null) {
            for (LineItemRecord lineItem : orderRecord.getQdca10proLineItems()) {
                storeServerSales.Qdca10proLineItems.add(toQdca10proLineItem(lineItem, storeServerSales));
                storeServerSales.server = "qdca10pro";
                storeServerSales.itemSales.add(toItemSales(lineItem,storeServerSales));
            }
        }
        storeServerSales.persist();
    }

    public static Qdca10LineItem toQdca10LineItem(LineItemRecord lineItemRecord, StoreServerSales storeServerSales) {
        Qdca10LineItem item = new Qdca10LineItem();
        item.setItem(lineItemRecord.getItem());
        item.setName(lineItemRecord.getName());
        item.setPrice(lineItemRecord.getPrice());
        item.setStoreServerSales(storeServerSales);
        return item;
    }

    public static Qdca10proLineItem toQdca10proLineItem(LineItemRecord lineItemRecord, StoreServerSales storeServerSales) {
        Qdca10proLineItem item = new Qdca10proLineItem();
        item.setItem(lineItemRecord.getItem());
        item.setName(lineItemRecord.getName());
        item.setPrice(lineItemRecord.getPrice());
        item.setStoreServerSales(storeServerSales);
        return item;
    }

    public static ItemSales toItemSales(LineItemRecord lineItemRecord, StoreServerSales storeServerSales) {
        ItemSales item = new ItemSales();
        item.setItem(lineItemRecord.getItem());
        item.setPrice(lineItemRecord.getPrice());
        item.setSalesTotal(1);
        item.setRevenue(lineItemRecord.getPrice());
        item.setStoreServerSales(storeServerSales);
        item.date = Instant.now();
        return item;
    }
}