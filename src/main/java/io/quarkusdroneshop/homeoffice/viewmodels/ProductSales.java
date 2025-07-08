package io.quarkusdroneshop.homeoffice.viewmodels;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Parameters;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkusdroneshop.homeoffice.domain.Item;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

