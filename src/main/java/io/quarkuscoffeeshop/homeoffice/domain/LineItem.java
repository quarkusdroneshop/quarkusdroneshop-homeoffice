package io.quarkuscoffeeshop.homeoffice.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.StringJoiner;

public class LineItem {

    private String id;

    private Item item;

    private BigDecimal price;

    public LineItem() {
    }

    public LineItem(String id, Item item, BigDecimal price) {
        this.id = id;
        this.item = item;
        this.price = price;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LineItem.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("item=" + item)
                .add("price=" + price)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineItem lineItem = (LineItem) o;
        return Objects.equals(id, lineItem.id) &&
                item == lineItem.item &&
                Objects.equals(price, lineItem.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, item, price);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
