package io.quarkusdroneshop.homeoffice.viewmodels;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkusdroneshop.homeoffice.domain.Item;
import io.quarkusdroneshop.homeoffice.domain.Order;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

// @Entity
// @Table(name="ProductItemSales")
// @RegisterForReflection
// public class ProductItemSales extends PanacheEntity {

//     @Enumerated(EnumType.STRING)
//     public Item item;

//     @Column(nullable = false)
//     public BigDecimal salesTotal;

//     @Column(nullable = false)
//     public Instant date;

//     @Column(nullable = false)
//     public BigDecimal revenue;

//     // 🔽 これを追加（親との関連）

//     @ManyToOne(optional = false)
//     public ProductSales productSales;

//     public ProductItemSales(){
//     }

//     public ProductItemSales(Item item, BigDecimal salesTotal, BigDecimal revenue){
//         this.item = item;
//         this.salesTotal = salesTotal;
//         this.revenue = revenue;
//     }

//     public ProductItemSales(Item item, BigDecimal salesTotal, BigDecimal revenue, Instant date){
//         this.item = item;
//         this.salesTotal = salesTotal;
//         this.date = date;
//         this.revenue = revenue;
//     }

//     public void setSalesTotal(BigDecimal salesTotal) {
//         this.salesTotal = salesTotal;
//     }

//     public BigDecimal getSalesTotal() {
//         return salesTotal;
//     }

//     public void setProductSales(ProductSales productSales) {
//         this.productSales = productSales;
//     }
// }
@Entity
@Table(name="ProductItemSales")
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