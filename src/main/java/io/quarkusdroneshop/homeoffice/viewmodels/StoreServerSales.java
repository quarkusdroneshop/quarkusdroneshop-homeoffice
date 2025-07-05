package io.quarkusdroneshop.homeoffice.viewmodels;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkusdroneshop.homeoffice.domain.Item;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.LineItemRecord;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.OrderRecord;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.BaristaLineItem;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.KitchenLineItem;


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
    public List<BaristaLineItem> baristaLineItems = new ArrayList<>();

    @OneToMany(mappedBy = "storeServerSales", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<KitchenLineItem> kitchenLineItems = new ArrayList<>();

    @OneToMany(mappedBy = "storeServerSales", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ItemSales> itemSales = new ArrayList<>();

    public static void persist(OrderRecord orderRecord) {
        StoreServerSales storeServerSales = new StoreServerSales();
        storeServerSales.store = "TOKYO";

        if (orderRecord.getBaristaLineItems() != null) {
            for (LineItemRecord lineItem : orderRecord.getBaristaLineItems()) {
                storeServerSales.baristaLineItems.add(toBaristaLineItem(lineItem));
                storeServerSales.server = "Barista";
                storeServerSales.itemSales.add(toItemSales(lineItem));
            }
        }
        if (orderRecord.getKitchenLineItems() != null) {
            for (LineItemRecord lineItem : orderRecord.getKitchenLineItems()) {
                storeServerSales.kitchenLineItems.add(toKitchenLineItem(lineItem));
                storeServerSales.server = "Kitchen";
                storeServerSales.itemSales.add(toItemSales(lineItem));
            }
        }

        storeServerSales.persist();
    }

    public static BaristaLineItem toBaristaLineItem(LineItemRecord lineItemRecord) {
        BaristaLineItem item = new BaristaLineItem();
        item.setItem(lineItemRecord.getItem());
        item.setName(lineItemRecord.getName());
        item.setPrice(lineItemRecord.getPrice());
        return item;
    }

    public static KitchenLineItem toKitchenLineItem(LineItemRecord lineItemRecord) {
        KitchenLineItem item = new KitchenLineItem();
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