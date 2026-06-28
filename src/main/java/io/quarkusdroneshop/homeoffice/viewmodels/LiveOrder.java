package io.quarkusdroneshop.homeoffice.viewmodels;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class LiveOrder {

    public String orderId;
    public String name;
    public String item;
    public String status;   // IN_QUEUE | IN_PROGRESS | FULFILLED
    public String madeBy;
    public String createdAt;
    public String updatedAt;

    public LiveOrder() {}

    public LiveOrder(String orderId, String name, String item,
                     String status, String madeBy,
                     String createdAt, String updatedAt) {
        this.orderId   = orderId;
        this.name      = name;
        this.item      = item;
        this.status    = status;
        this.madeBy    = madeBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getOrderId()   { return orderId; }
    public String getName()      { return name; }
    public String getItem()      { return item; }
    public String getStatus()    { return status; }
    public String getMadeBy()    { return madeBy; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
