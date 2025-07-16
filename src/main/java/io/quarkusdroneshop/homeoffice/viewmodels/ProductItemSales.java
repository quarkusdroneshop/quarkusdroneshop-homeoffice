package io.quarkusdroneshop.homeoffice.viewmodels;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkusdroneshop.homeoffice.domain.Item;
import io.quarkusdroneshop.homeoffice.domain.Order;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name="productitemsales")
public class ProductItemSales extends PanacheEntity {

    public String name;
    public BigDecimal salesTotal;
    public BigDecimal revenue;
    public Instant saleDate;
    public String preparedBy;

    @ManyToOne
    @JoinColumn(name = "product_sales_id")
    public ProductSales productSales;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Item item;

    public ProductItemSales() {}

    public void setProductSales(ProductSales productSales) {
        this.productSales = productSales;
    }

    public ProductItemSales(Item item, BigDecimal salesTotal, BigDecimal revenue, Instant saleDate) {
        this.item = item;
        this.salesTotal = salesTotal;
        this.revenue = revenue;
        this.saleDate = saleDate;
    }

    public void setSalesTotal(BigDecimal salesTotal) {
        this.salesTotal = salesTotal;
    }
    public void setPreparedBy(String preparedBy) {
        this.preparedBy = preparedBy;
    }
}