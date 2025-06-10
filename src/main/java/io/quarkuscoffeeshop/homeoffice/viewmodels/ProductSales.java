package io.quarkuscoffeeshop.homeoffice.viewmodels;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Parameters;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkuscoffeeshop.homeoffice.domain.Item;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="ProductSales")
public class ProductSales extends PanacheEntity {

    @Enumerated(EnumType.STRING)
    public Item item;

    public Instant createdTimestamp;

    @OneToMany(targetEntity=ProductItemSales.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ProductItemSales> productItemSales;

    public ProductSales() {
        productItemSales = new ArrayList<>();
    }

    public ProductSales(Item item) {
        this.item = item;
        this.productItemSales = new ArrayList<>();
        this.createdTimestamp = Instant.now();
    }

    public ProductSales(Item item, List<ProductItemSales> sales){
        this.item = item;
        this.productItemSales = sales;
        this.createdTimestamp = Instant.now();
    }

    public static ProductSales findByItem(Item item){
        ProductSales productSales =  ProductSales.find("item = :item order by id desc", Parameters.with("item", item)).firstResult();
        if (productSales == null){
            productSales = new ProductSales(item, new ArrayList<ProductItemSales>());
        }
        return productSales;
    }

    public Item getItem(){
        return this.item;
    }
}
