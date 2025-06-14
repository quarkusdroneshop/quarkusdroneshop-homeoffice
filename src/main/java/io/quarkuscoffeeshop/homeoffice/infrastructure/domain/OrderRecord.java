package io.quarkuscoffeeshop.homeoffice.infrastructure.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.List;
import io.quarkuscoffeeshop.homeoffice.domain.OrderSource;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.LineItemRecord;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;


@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRecord {

    
    @JsonProperty("baristaLineItems")
    private List<LineItemRecord> baristaLineItems;
    
    @JsonProperty("kitchenLineItems")
    private List<LineItemRecord> kitchenLineItems;
    
    @JsonProperty("price")
    private double total;

    @JsonProperty("orderSource")
    private OrderSource orderSource;

    @JsonProperty("location")
    private String location;

    @JsonProperty("id")
    private String externalOrderId;

    @JsonProperty("orderCompletedTimestamp")
    public String CompletedOrderId;

    @JsonProperty("loyaltyMemberId")
    private String customerLoyaltyId;

    @JsonProperty("orderId")
    private String orderId;

    private Instant orderPlacedTimestamp;
    private Instant orderCompletedTimestamp;
    public Instant timestamp;

    public OrderRecord() {
    }

    public String orderId() { return orderId; }
    public List<LineItemRecord> getBaristaLineItems() { return baristaLineItems; }
    public List<LineItemRecord> getKitchenLineItems() { return kitchenLineItems; }
    public double total() { return total; }
    public OrderSource orderSource() { return orderSource; }
    public String location() { return location; }
    public String externalOrderId() { return externalOrderId; }
    public String customerLoyaltyId() { return customerLoyaltyId; }
    public Instant orderPlacedTime() { return orderPlacedTimestamp; }
    public Instant orderCompletedTime() { return orderCompletedTimestamp; }

    @Override
    public String toString() {
        return "OrderRecord{" +
                "orderId='" + orderId + '\'' +
                ", baristaLineItems=" + baristaLineItems +
                ", kitchenLineItems=" + kitchenLineItems +
                ", total=" + total +
                ", orderSource=" + orderSource +
                ", location='" + location + '\'' +
                ", externalOrderId='" + externalOrderId + '\'' +
                ", customerLoyaltyId='" + customerLoyaltyId + '\'' +
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

    public String getOrderId() {
        return externalOrderId != null ? externalOrderId : orderId;
    }

    public String setderId() {
        return orderId;
    }

    public String getExternalOrderId() {
        return externalOrderId;
    }

}