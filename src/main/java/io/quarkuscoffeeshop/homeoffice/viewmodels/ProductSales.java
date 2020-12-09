package io.quarkuscoffeeshop.homeoffice.viewmodels;
import io.quarkuscoffeeshop.homeoffice.domain.Item;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ProductSales {

    public Item item;

    public List<ItemSales> sales;

    public ProductSales(){
        sales = new ArrayList<>();
    }

    public ProductSales(Item item, List<ItemSales> sales){
        this.item = item;
        this.sales = sales;
    }

}
