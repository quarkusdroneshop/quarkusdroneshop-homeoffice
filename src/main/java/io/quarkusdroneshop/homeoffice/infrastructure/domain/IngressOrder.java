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
    private final List<IngressLineItem> Qdca10LineItems;
    private final List<IngressLineItem> Qdca10proLineItems;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public IngressOrder(
            @JsonProperty("id") String orderId,
            @JsonProperty("orderSource") OrderSource orderSource,
            @JsonProperty("location") String location,
            @JsonProperty("loyaltyMemberId") String loyaltyMemberId,
            @JsonProperty("timestamp") Instant timestamp,
            @JsonProperty("eventType") EventType eventType,
            @JsonProperty("Qdca10LineItems") List<IngressLineItem> Qdca10LineItems,
            @JsonProperty("Qdca10proLineItems") List<IngressLineItem> Qdca10proLineItems) {
        this.orderId = orderId;
        this.orderSource = orderSource;
        this.location = location;
        this.loyaltyMemberId = loyaltyMemberId;
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.Qdca10LineItems = Qdca10LineItems;
        this.Qdca10proLineItems = Qdca10proLineItems;
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

    public Optional<List<IngressLineItem>> getQdca10LineItems() {
        return Optional.ofNullable(Qdca10LineItems);
    }

    public Optional<List<IngressLineItem>> getKitßchenLineItems() {
        return Optional.ofNullable(Qdca10proLineItems);
    }
}