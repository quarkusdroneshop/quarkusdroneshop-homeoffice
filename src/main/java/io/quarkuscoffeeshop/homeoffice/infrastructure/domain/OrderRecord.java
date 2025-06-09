package io.quarkuscoffeeshop.homeoffice.infrastructure.domain;

import io.quarkuscoffeeshop.homeoffice.domain.OrderSource;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class OrderRecord {
    private String orderId;
    private OrderSource orderSource;
    private EventType eventType;
    private String loyaltyMemberId;
    private Instant timestamp;
    private String externalOrderId;
    private List<LineItemRecord> baristaLineItems;
    private List<LineItemRecord> kitchenLineItems;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    public Instant timeIn;
    public Instant timeUp;

    // コンストラクタ
    public OrderRecord(String orderId, OrderSource orderSource, EventType eventType, String loyaltyMemberId, String externalOrderId,
                       Instant timestamp, List<LineItemRecord> baristaLineItems, List<LineItemRecord> kitchenLineItems) {
        this.orderId = orderId;
        this.orderSource = orderSource;
        this.eventType = eventType;
        this.externalOrderId = externalOrderId;
        this.loyaltyMemberId = loyaltyMemberId;
        this.timestamp = timestamp;
        this.baristaLineItems = baristaLineItems;
        this.kitchenLineItems = kitchenLineItems;
    }

    // Getter
    public String orderId() { return orderId; }
    public OrderSource orderSource() { return orderSource; }
    public String externalOrderId() { return externalOrderId; }
    public EventType eventType() { return eventType; }
    public String loyaltyMemberId() { return orderId; }
    public Instant timestamp() { return timestamp; }
    public List<LineItemRecord> baristaLineItems() { return baristaLineItems; }
    public List<LineItemRecord> kitchenLineItems() { return kitchenLineItems; }

    public Instant timeIn() { return timeIn; }
    public Instant timeUp() { return timeUp; }

    // Setter も必要なら追加（record ではできなかった mutable な操作に対応）
    public void setTimeIn(Instant timeIn) { this.timeIn = timeIn; }
    public void setTimeUp(Instant timeUp) { this.timeUp = timeUp; }
    public void setExternalOrderId(String externalOrderId) { this.externalOrderId = externalOrderId; }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    // toString() も log出力のために定義しておくとよい
    @Override
    public String toString() {
        return "OrderRecord{startTime=" + startTime + ", endTime=" + endTime + "}";
    }
}