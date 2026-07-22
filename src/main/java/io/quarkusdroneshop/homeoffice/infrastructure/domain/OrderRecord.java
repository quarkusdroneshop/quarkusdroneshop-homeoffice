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

    @JsonProperty("qdca10LineItems")
    private List<LineItemRecord> qdca10LineItems;
    
    @JsonProperty("qdca10proLineItems")
    private List<LineItemRecord> qdca10proLineItems;
    
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
    public List<LineItemRecord> getQdca10LineItems() { return qdca10LineItems; }
    public List<LineItemRecord> getQdca10proLineItems() { return qdca10proLineItems; }
    public double total() { return total; }
    public OrderSource orderSource() { return orderSource; }
    public String location() { return location; }
    public String externalOrderId() { return externalOrderId; }
    public String customerLoyaltyId() { return customerLoyalty; }
    public Instant orderPlacedTime() { return orderPlacedTimestamp; }
    public Instant orderCompletedTime() { return orderCompletedTimestamp; }

    // dataproduct-order-events (明細単位のストリーム) からオンメモリで組み立てるために
    // 追加した setter 群。Jackson デシリアライズ以外の経路 (OrderAssemblyAggregator) から
    // プログラム的に OrderRecord を構築する用途。
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setQdca10LineItems(List<LineItemRecord> qdca10LineItems) { this.qdca10LineItems = qdca10LineItems; }
    public void setQdca10proLineItems(List<LineItemRecord> qdca10proLineItems) { this.qdca10proLineItems = qdca10proLineItems; }
    public void setTotal(double total) { this.total = total; }
    public void setOrderSource(OrderSource orderSource) { this.orderSource = orderSource; }
    public void setLocation(String location) { this.location = location; }
    public void setExternalOrderId(String externalOrderId) { this.externalOrderId = externalOrderId; }
    public void setCustomerLoyaltyId(String customerLoyalty) { this.customerLoyalty = customerLoyalty; }

    @Override
    public String toString() {
        return "OrderRecord{" +
                "orderId='" + externalOrderId + '\'' +
                ", Qdca10LineItems=" + qdca10LineItems +
                ", Qdca10proLineItems=" + qdca10proLineItems +
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

    public String setderId() {
        return externalOrderId;
    }

    public String getExternalOrderId() {
        return externalOrderId;
    }

}