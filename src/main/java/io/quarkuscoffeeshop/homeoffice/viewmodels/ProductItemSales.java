package io.quarkuscoffeeshop.homeoffice.viewmodels;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkuscoffeeshop.homeoffice.domain.Item;
import io.quarkuscoffeeshop.homeoffice.domain.Order;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name="ProductItemSales")
@RegisterForReflection
public class ProductItemSales extends PanacheEntity {

    @Enumerated(EnumType.STRING)
    public Item item;

    public long salesTotal;

    public Instant date;

    public BigDecimal revenue;

    public ProductItemSales(){
    }

    public ProductItemSales(Item item, long salesTotal, BigDecimal revenue){
        this.item = item;
        this.salesTotal = salesTotal;
        this.revenue = revenue;
    }

    public ProductItemSales(Item item, long salesTotal, BigDecimal revenue, Instant date){
        this.item = item;
        this.salesTotal = salesTotal;
        this.date = date;
        this.revenue = revenue;
    }
}
