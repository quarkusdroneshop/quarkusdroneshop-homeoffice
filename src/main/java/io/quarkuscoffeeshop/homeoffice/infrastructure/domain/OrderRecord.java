package io.quarkuscoffeeshop.homeoffice.infrastructure.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.List;
import io.quarkuscoffeeshop.homeoffice.domain.OrderSource;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.LineItemRecord;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRecord {


    private String orderId;
    
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

    @JsonProperty("loyaltyMemberId")
    private String customerLoyaltyId;
    
    private Instant orderPlacedTimestamp;
    private Instant orderCompletedTimestamp;

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
    public Instant orderPlacedTimestamp() { return orderPlacedTimestamp; }
    public Instant orderCompletedTimestamp() { return orderCompletedTimestamp; }
    
    public Instant timestamp() {
        return orderPlacedTimestamp; // または orderCompletedTimestamp など必要に応じて
    }

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
}