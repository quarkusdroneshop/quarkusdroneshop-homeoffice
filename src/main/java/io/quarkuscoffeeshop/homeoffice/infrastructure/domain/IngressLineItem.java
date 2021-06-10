package io.quarkuscoffeeshop.homeoffice.infrastructure.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkuscoffeeshop.homeoffice.domain.Item;
import io.quarkuscoffeeshop.homeoffice.domain.Order;

public class IngressLineItem {

    private final Item item;

    private final String name;

    @JsonIgnore
    private final Order order;

    public IngressLineItem(Item item, String name, Order order) {
        this.item = item;
        this.name = name;
        this.order = order;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IngressLineItem{");
        sb.append("item=").append(item);
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IngressLineItem that = (IngressLineItem) o;

        if (item != that.item) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return order != null ? order.equals(that.order) : that.order == null;
    }

    @Override
    public int hashCode() {
        int result = item != null ? item.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (order != null ? order.hashCode() : 0);
        return result;
    }

    public Item getItem() {
        return item;
    }

    public String getName() {
        return name;
    }

    public Order getOrder() {
        return order;
    }
}
