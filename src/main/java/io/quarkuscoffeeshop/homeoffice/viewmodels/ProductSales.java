package io.quarkuscoffeeshop.homeoffice.viewmodels;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Parameters;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkuscoffeeshop.homeoffice.domain.Item;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//@Entity
//@Table(name="ProductSales")
// public class ProductSales extends PanacheEntity {

//     @Enumerated(EnumType.STRING)
//     public Item item;

//     public Instant createdTimestamp;

//     @OneToMany(mappedBy = "productSales", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
//     public List<ProductItemSales> productItemSales = new ArrayList<>();

//     public ProductSales() {
//         productItemSales = new ArrayList<>();
//     }

//     public ProductSales(Item item) {
//         this.item = item;
//         this.productItemSales = new ArrayList<>();
//         this.createdTimestamp = Instant.now();
//     }

//     public ProductSales(Item item, List<ProductItemSales> sales){
//         this.item = item;
//         this.productItemSales = sales;
//         this.createdTimestamp = Instant.now();
//     }

//     public void addProductItemSale(ProductItemSales itemSale) {
//         itemSale.productSales = this; // ここが重要！
//         this.productItemSales.add(itemSale);
//     }

//     // public static ProductSales findByItem(Item item){
//     //     // ProductSales productSales =  ProductSales.find("item = :item order by id desc", Parameters.with("item", item)).firstResult();
//     //     // if (productSales == null){
//     //     //     productSales = new ProductSales(item, new ArrayList<ProductItemSales>());
//     //     // }
//     //     // return productSales;
//     //     return find("item = :item order by id desc", Parameters.with("item", item)).firstResult();
//     // }
//     public static ProductSales findByItem(Item item){
//         ProductSales productSales = find("item = :item order by id desc", Parameters.with("item", item)).firstResult();
//         if (productSales == null){
//             productSales = new ProductSales(item);
//         }
//         return productSales;
//     }

//     public Item getItem(){
//         return this.item;
//     }
// }

@Entity
@Table(name="ProductSales")
public class ProductSales extends PanacheEntity {

    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    public Item item;

    @Column(nullable = false, updatable = false)
    public Instant createdTimestamp;

    @PrePersist
    public void onCreate() {
        this.createdTimestamp = Instant.now();
    }

    @OneToMany(mappedBy = "productSales", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ProductItemSales> productItemSales = new ArrayList<>();

    public static ProductSales findByItem(Item item) {
        return find("item", item).firstResult();
    }

    public ProductSales() {}

    public ProductSales(Item item) {
        this.item = item;
    }

    public void addProductItemSale(ProductItemSales itemSales) {
        productItemSales.add(itemSales);
        itemSales.setProductSales(this);
    }

    public Item getItem() {
        return this.item;
    }
}

