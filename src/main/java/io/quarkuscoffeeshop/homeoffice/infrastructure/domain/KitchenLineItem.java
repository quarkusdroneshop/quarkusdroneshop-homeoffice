package io.quarkuscoffeeshop.homeoffice.infrastructure.domain;

import javax.persistence.GeneratedValue;
import io.quarkuscoffeeshop.homeoffice.domain.Item;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;


@Entity
public class KitchenLineItem {
    @Id
    @GeneratedValue
    public Long id;

    public Item item;
    public Double price;
    public String name;

        public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}