package io.quarkusdroneshop.homeoffice.infrastructure.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.List;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkusdroneshop.homeoffice.domain.OrderSource;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.LineItemRecord;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRecord {

    
    @JsonProperty("Qdca10LineItems")
    private List<LineItemRecord> Qdca10LineItems;
    
    @JsonProperty("Qdca10proLineItems")
    private List<LineItemRecord> Qdca10proLineItems;
    
    @JsonProperty("price")
    private double total;

    @JsonProperty("orderSource")
    private OrderSource orderSource;

    @JsonProperty("location")
    private String location;

    @JsonProperty("id")
    private String externalOrderId;

    @JsonProperty("orderCompletedTimestamp")
    public Instant orderCompletedTimestamp;

    @JsonProperty("orderPlacedTimestamp")
    public Instant orderPlacedTimestamp;

    @JsonProperty("loyaltyMemberId")
    private String customerLoyalty;

    @JsonProperty("orderId")
    private String orderId;

    public Instant timestamp;

    public OrderRecord() {
    }

    public String orderId() { return orderId; }
    public List<LineItemRecord> getQdca10LineItems() { return Qdca10LineItems; }
    public List<LineItemRecord> getQdca10proLineItems() { return Qdca10proLineItems; }
    public double total() { return total; }
    public OrderSource orderSource() { return orderSource; }
    public String location() { return location; }
    public String externalOrderId() { return externalOrderId; }
    public String customerLoyaltyId() { return customerLoyalty; }
    public Instant orderPlacedTime() { return orderPlacedTimestamp; }
    public Instant orderCompletedTime() { return orderCompletedTimestamp; }

    @Override
    public String toString() {
        return "OrderRecord{" +
                "orderId='" + externalOrderId + '\'' +
                ", Qdca10LineItems=" + Qdca10LineItems +
                ", Qdca10proLineItems=" + Qdca10proLineItems +
                ", total=" + total +
                ", orderSource=" + orderSource +
                ", location='" + location + '\'' +
                ", externalOrderId='" + externalOrderId + '\'' +
                ", customerLoyaltyId='" + customerLoyalty + '\'' +
                ", orderPlacedTimestamp=" + orderPlacedTimestamp +
                ", orderCompletedTimestamp=" + orderCompletedTimestamp +
                '}';
    }

    public Instant orderPlacedTimestamp() {
        return orderPlacedTimestamp;
    }

    public Instant orderCompletedTimestamp() {
        return orderCompletedTimestamp;
    }

    public Instant timestamp() {
        return timestamp;
    }

    // public String getOrderId() {
    //     return externalOrderId != null ? externalOrderId : externalOrderId;
    // }

    public String setderId() {
        return externalOrderId;
    }

    public String getExternalOrderId() {
        return externalOrderId;
    }

}