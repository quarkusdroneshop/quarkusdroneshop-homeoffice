package io.quarkuscoffeeshop.homeoffice.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.graphql.api.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.transaction.Transactional;

import java.beans.Transient;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Entity 
@Table(name="Orders") 
@RegisterForReflection
public class Order extends PanacheEntityBase {

    //@Transient
    static Logger logger = LoggerFactory.getLogger(Order.class);

    @Id
    private String orderId;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "order", cascade = CascadeType.ALL)
    private List<LineItem> lineItems;

    BigDecimal total;

    Item item;

    @Enumerated(EnumType.STRING)
    OrderSource orderSource;

    //@Enumerated(EnumType.STRING)
    //StoreLocation location;
    String location;

    String customerLoyaltyId;

    Instant orderPlacedTimestamp;

    Instant orderCompletedTimestamp;

    Instant createdTimestamp;

    public Order() {
    }

    public Order(String orderId, List<LineItem> lineItems, OrderSource orderSource, String location, String customerLoyaltyId, Instant orderPlacedTimestamp, Instant orderCompletedTimestamp) {
        this.orderId = orderId;
        lineItems.forEach(lineItem -> {
            addLineItem(lineItem);
        });
        this.orderSource = orderSource;
        this.location = location;
        this.customerLoyaltyId = customerLoyaltyId;
        this.orderPlacedTimestamp = orderPlacedTimestamp;
        this.orderCompletedTimestamp = orderCompletedTimestamp;
        this.total = lineItems
                .stream()
                .map(item -> item.getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.setCreatedTimestamp();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Order.class.getSimpleName() + "[", "]")
                .add("orderId='" + orderId + "'")
                .add("lineItems=" + lineItems)
                .add("total=" + total)
                .add("orderSource=" + orderSource)
                .add("location='" + location + "'")
                .add("customerLoyaltyId='" + customerLoyaltyId + "'")
                .add("orderPlacedTimestamp=" + orderPlacedTimestamp)
                .add("orderCompletedTimestamp=" + orderCompletedTimestamp)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        if (orderId != null ? !orderId.equals(order.orderId) : order.orderId != null) return false;
        if (lineItems != null ? !lineItems.equals(order.lineItems) : order.lineItems != null) return false;
        if (total != null ? !total.equals(order.total) : order.total != null) return false;
        if (orderSource != order.orderSource) return false;
        if (location != null ? !location.equals(order.location) : order.location != null) return false;
        if (customerLoyaltyId != null ? !customerLoyaltyId.equals(order.customerLoyaltyId) : order.customerLoyaltyId != null)
            return false;
        if (orderPlacedTimestamp != null ? !orderPlacedTimestamp.equals(order.orderPlacedTimestamp) : order.orderPlacedTimestamp != null)
            return false;
        return orderCompletedTimestamp != null ? orderCompletedTimestamp.equals(order.orderCompletedTimestamp) : order.orderCompletedTimestamp == null;
    }

    @Override
    public int hashCode() {
        int result = orderId != null ? orderId.hashCode() : 0;
        result = 31 * result + (lineItems != null ? lineItems.hashCode() : 0);
        result = 31 * result + (total != null ? total.hashCode() : 0);
        result = 31 * result + (orderSource != null ? orderSource.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (customerLoyaltyId != null ? customerLoyaltyId.hashCode() : 0);
        result = 31 * result + (orderPlacedTimestamp != null ? orderPlacedTimestamp.hashCode() : 0);
        result = 31 * result + (orderCompletedTimestamp != null ? orderCompletedTimestamp.hashCode() : 0);
        return result;
    }

    public void addLineItem(final LineItem lineItem) {
        if (this.lineItems == null) {
            this.lineItems = new ArrayList<>();
        }
        this.lineItems.add(new LineItem(lineItem.getItem(), lineItem.getPrice(), lineItem.getPreparedBy(), this));
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Collection<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public Item getItem() {
        return item;
    }
    
    public Instant getCreatedAt() {
        return createdTimestamp;
    }; 

    public OrderSource getOrderSource() {
        return orderSource;
    }

    public void setCreatedTimestamp() {
        this.createdTimestamp = Instant.now();
    }
    public void setOrderSource(OrderSource orderSource) {
        this.orderSource = orderSource;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCustomerLoyaltyId() {
        return customerLoyaltyId;
    }

    public void setCustomerLoyaltyId(String customerLoyaltyId) {
        this.customerLoyaltyId = customerLoyaltyId;
    }

    public Instant getOrderPlacedTimestamp() {
        return orderPlacedTimestamp;
    }

    public void setOrderPlacedTimestamp(Instant orderPlacedTimestamp) {
        this.orderPlacedTimestamp = orderPlacedTimestamp;
    }

    public Instant getOrderCompletedTimestamp() {
        return orderCompletedTimestamp;
    }

    public void setOrderCompletedTimestamp(Instant orderCompletedTimestamp) {
        this.orderCompletedTimestamp = orderCompletedTimestamp;
    }

    public static List<Order> findBetween(Instant startDate, Instant endDate) {
        //logger.debug("Searching date between: {} and {}", startDate, endDate);
        return find("orderPlacedTimestamp BETWEEN :startDate AND :endDate",
                Parameters.with("startDate", startDate)
                .and("endDate", endDate)
        ).list();
    }

    public static List<Order> findBetweenAfter(Instant startDate, Instant endDate, Instant createdTimestamp) {
        //logger.debug("Searching date between: {} and {}", startDate, endDate);
        return find("orderPlacedTimestamp BETWEEN :startDate AND :endDate AND createdTimestamp >= :createdTimestamp",
                Parameters.with("startDate", startDate)
                        .and("endDate", endDate)
                        .and("createdTimestamp", createdTimestamp)
        ).list();
    }

    public static List<Order> findBetweenForLocation(Store location, Instant startDate, Instant endDate) {
        //logger.debug("Searching date between: {} and {} for {}", startDate, endDate, location);
        return find("location = :location AND orderPlacedTimestamp BETWEEN :startDate AND :endDate",
                Parameters.with("location", location)
                        .and("startDate", startDate)
                        .and("endDate", endDate)
        ).list();
    }
}
