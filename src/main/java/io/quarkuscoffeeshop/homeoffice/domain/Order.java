package io.quarkuscoffeeshop.homeoffice.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;
import java.util.StringJoiner;

@RegisterForReflection
public class Order {

    private String id;

    private Collection<LineItem> lineItems;

    BigDecimal total;

    OrderSource orderSource;

    String locationId;

    public Order() {
    }

    public Order(String id, Collection<LineItem> lineItems, BigDecimal total) {
        this.id = id;
        this.lineItems = lineItems;
        this.total = total;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Order.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("lineItems=" + lineItems)
                .add("total=" + total)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id) &&
                Objects.equals(lineItems, order.lineItems) &&
                Objects.equals(total, order.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lineItems, total);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Collection<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(Collection<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
