package io.quarkusdroneshop.homeoffice.viewmodels;

public class AssemblyLeadTime {

    public String itemId;
    public String orderId;
    public String item;
    public long placedAt;
    public long fulfilledAt;
    public long leadTimeSeconds;
    public String assemblyLine;

    public AssemblyLeadTime() {
    }

    public AssemblyLeadTime(String itemId, String orderId, String item, long placedAt, long fulfilledAt,
            long leadTimeSeconds, String assemblyLine) {
        this.itemId = itemId;
        this.orderId = orderId;
        this.item = item;
        this.placedAt = placedAt;
        this.fulfilledAt = fulfilledAt;
        this.leadTimeSeconds = leadTimeSeconds;
        this.assemblyLine = assemblyLine;
    }
}
