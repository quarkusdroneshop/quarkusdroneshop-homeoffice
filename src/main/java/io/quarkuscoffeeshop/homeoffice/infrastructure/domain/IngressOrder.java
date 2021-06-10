package io.quarkuscoffeeshop.homeoffice.infrastructure.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.quarkuscoffeeshop.homeoffice.domain.OrderSource;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@JsonRootName("order")
public class IngressOrder {

    private final String orderId;

    private final OrderSource orderSource;

    private final EventType eventType;

    private final String loyaltyMemberId;

    private final Instant timestamp;

    private final List<IngressLineItem> baristaLineItems;

    private final List<IngressLineItem> kitchenLineItems;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public IngressOrder(
            @JsonProperty("orderId") String orderId,
            @JsonProperty("orderSource") OrderSource orderSource,
            @JsonProperty("eventType") EventType eventType,
            @JsonProperty("loyaltyMemberId") String loyaltyMemberId,
            @JsonProperty("timestamp") Instant timestamp,
            @JsonProperty("baristaLineItems") List<IngressLineItem> baristaLineItems,
            @JsonProperty("kitchenLineItems") List<IngressLineItem> kitchenLineItems) {
        this.orderId = orderId;
        this.orderSource = orderSource;
        this.eventType = eventType;
        this.loyaltyMemberId = loyaltyMemberId;
        this.timestamp = timestamp;
        this.baristaLineItems = baristaLineItems;
        this.kitchenLineItems = kitchenLineItems;
    }

    public Optional<List<IngressLineItem>> getBaristaLineItems() {
        return Optional.ofNullable(this.baristaLineItems);
    }

    public Optional<List<IngressLineItem>> getKitchenLineItems() {
        return Optional.ofNullable(this.kitchenLineItems);
    }

    public Optional<String> getLoyaltyMemberId() {
        return Optional.ofNullable(this.loyaltyMemberId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IngressOrder{");
        sb.append("orderId='").append(orderId).append('\'');
        sb.append(", orderSource='").append(orderSource).append('\'');
        sb.append(", eventType=").append(eventType);
        sb.append(", loyaltyMemberId='").append(loyaltyMemberId).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", baristaLineItems=").append(baristaLineItems);
        sb.append(", kitchenLineItems=").append(kitchenLineItems);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IngressOrder that = (IngressOrder) o;

        if (orderId != null ? !orderId.equals(that.orderId) : that.orderId != null) return false;
        if (orderSource != null ? !orderSource.equals(that.orderSource) : that.orderSource != null) return false;
        if (eventType != that.eventType) return false;
        if (loyaltyMemberId != null ? !loyaltyMemberId.equals(that.loyaltyMemberId) : that.loyaltyMemberId != null)
            return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) return false;
        if (baristaLineItems != null ? !baristaLineItems.equals(that.baristaLineItems) : that.baristaLineItems != null)
            return false;
        return kitchenLineItems != null ? kitchenLineItems.equals(that.kitchenLineItems) : that.kitchenLineItems == null;
    }

    @Override
    public int hashCode() {
        int result = orderId != null ? orderId.hashCode() : 0;
        result = 31 * result + (orderSource != null ? orderSource.hashCode() : 0);
        result = 31 * result + (eventType != null ? eventType.hashCode() : 0);
        result = 31 * result + (loyaltyMemberId != null ? loyaltyMemberId.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (baristaLineItems != null ? baristaLineItems.hashCode() : 0);
        result = 31 * result + (kitchenLineItems != null ? kitchenLineItems.hashCode() : 0);
        return result;
    }

    public String getOrderId() {
        return orderId;
    }

    public OrderSource getOrderSource() {
        return orderSource;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
