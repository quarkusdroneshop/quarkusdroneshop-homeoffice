package io.quarkusdroneshop.homeoffice.infrastructure.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkusdroneshop.homeoffice.domain.OrderSource;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class IngressOrder {

    private final String orderId;
    private final OrderSource orderSource;
    private final String location;
    private final String loyaltyMemberId;
    private final Instant timestamp;
    private final EventType eventType;
    private final List<IngressLineItem> QDCA10LineItems;
    private final List<IngressLineItem> QDCA10ProLineItems;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public IngressOrder(
            @JsonProperty("id") String orderId,
            @JsonProperty("orderSource") OrderSource orderSource,
            @JsonProperty("location") String location,
            @JsonProperty("loyaltyMemberId") String loyaltyMemberId,
            @JsonProperty("timestamp") Instant timestamp,
            @JsonProperty("eventType") EventType eventType,
            @JsonProperty("QDCA10LineItems") List<IngressLineItem> QDCA10LineItems,
            @JsonProperty("QDCA10ProLineItems") List<IngressLineItem> QDCA10ProLineItems) {
        this.orderId = orderId;
        this.orderSource = orderSource;
        this.location = location;
        this.loyaltyMemberId = loyaltyMemberId;
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.QDCA10LineItems = QDCA10LineItems;
        this.QDCA10ProLineItems = QDCA10ProLineItems;
    }

    public String getOrderId() {
        return orderId;
    }

    public OrderSource getOrderSource() {
        return orderSource;
    }

    public String getLocation() {
        return location;
    }

    public Optional<String> getLoyaltyMemberId() {
        return Optional.ofNullable(loyaltyMemberId);
    }

    public Optional<Instant> getTimestamp() {
        return Optional.ofNullable(timestamp);
    }

    public Optional<EventType> getEventType() {
        return Optional.ofNullable(eventType);
    }

    public Optional<List<IngressLineItem>> getQDCA10LineItems() {
        return Optional.ofNullable(QDCA10LineItems);
    }

    public Optional<List<IngressLineItem>> getKitßchenLineItems() {
        return Optional.ofNullable(QDCA10ProLineItems);
    }
}