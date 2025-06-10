package io.quarkuscoffeeshop.homeoffice.viewmodels;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkuscoffeeshop.homeoffice.domain.Item;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.LineItemRecord;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.OrderRecord;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.BaristaLineItem;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.KitchenLineItem;


import javax.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "StoreServerSales")
public class StoreServerSales extends PanacheEntity {

    public String store;
    public String server;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "store_sales_id")
    public List<BaristaLineItem> baristaLineItems = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "store_sales_id")
    public List<KitchenLineItem> kitchenLineItems = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
        name = "storeserversales_itemsales",
        joinColumns = @JoinColumn(name = "store_server_sales_id"),
        inverseJoinColumns = @JoinColumn(name = "item_sales_id")
    )
    public List<ItemSales> itemSales = new ArrayList<>();

    public static void persist(OrderRecord orderRecord) {
        StoreServerSales storeServerSales = new StoreServerSales();
        storeServerSales.store = "TOKYO";
        storeServerSales.server = "OpenShift";

        if (orderRecord.getBaristaLineItems() != null) {
            for (LineItemRecord lineItem : orderRecord.getBaristaLineItems()) {
                storeServerSales.baristaLineItems.add(toBaristaLineItem(lineItem));
                storeServerSales.itemSales.add(toItemSales(lineItem));
            }
        }
        if (orderRecord.getKitchenLineItems() != null) {
            for (LineItemRecord lineItem : orderRecord.getKitchenLineItems()) {
                storeServerSales.kitchenLineItems.add(toKitchenLineItem(lineItem));
                storeServerSales.itemSales.add(toItemSales(lineItem));
            }
        }

        storeServerSales.persist();
    }

    public static BaristaLineItem toBaristaLineItem(LineItemRecord lineItemRecord) {
        BaristaLineItem item = new BaristaLineItem();
        item.setItem(lineItemRecord.getItem());
        item.setName(lineItemRecord.getPreparedBy());
        item.setPrice(lineItemRecord.getPrice());
        return item;
    }

    public static KitchenLineItem toKitchenLineItem(LineItemRecord lineItemRecord) {
        KitchenLineItem item = new KitchenLineItem();
        item.setItem(lineItemRecord.getItem());
        item.setName(lineItemRecord.getPreparedBy());
        item.setPrice(lineItemRecord.getPrice());
        return item;
    }

    public static ItemSales toItemSales(LineItemRecord lineItemRecord) {
        ItemSales item = new ItemSales();
        item.setItem(lineItemRecord.getItem());
        item.setPreparedBy(lineItemRecord.getPreparedBy());
        item.setPrice(lineItemRecord.getPrice());
        item.setSalesTotal(1);
        item.setRevenue(lineItemRecord.getPrice());
        return item;
    }
}