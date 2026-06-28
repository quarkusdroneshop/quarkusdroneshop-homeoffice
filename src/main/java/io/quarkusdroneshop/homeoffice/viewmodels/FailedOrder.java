package io.quarkusdroneshop.homeoffice.viewmodels;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class FailedOrder {

    public String orderId;
    public String name;
    public String item;
    public String failureReason;
    public String failedAt;
    public int retryCount;

    public FailedOrder() {}

    public FailedOrder(String orderId, String name, String item,
                       String failureReason, String failedAt, int retryCount) {
        this.orderId       = orderId;
        this.name          = name;
        this.item          = item;
        this.failureReason = failureReason;
        this.failedAt      = failedAt;
        this.retryCount    = retryCount;
    }

    public String getOrderId()       { return orderId; }
    public String getName()          { return name; }
    public String getItem()          { return item; }
    public String getFailureReason() { return failureReason; }
    public String getFailedAt()      { return failedAt; }
    public int    getRetryCount()    { return retryCount; }
}
