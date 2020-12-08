package io.quarkuscoffeeshop.homeoffice.viewmodels;
import io.quarkuscoffeeshop.homeoffice.domain.Item;

import java.math.BigDecimal;
import java.util.Date;

public class ItemSales {

    public Item item;

    public long sales;

    public BigDecimal revenue;

    public ItemSales(){

    }

    public ItemSales(Item item, long sales, BigDecimal revenue){
        this.item = item;
        this.sales = sales;
        this.revenue = revenue;
    }
}
