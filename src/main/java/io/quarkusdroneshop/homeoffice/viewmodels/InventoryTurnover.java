package io.quarkusdroneshop.homeoffice.viewmodels;

public class InventoryTurnover {

    public String item;
    public long windowStart;
    public long windowEnd;
    public long restockRequestedCount;
    public long stockoutCancelledCount;

    public InventoryTurnover() {
    }

    public InventoryTurnover(String item, long windowStart, long windowEnd, long restockRequestedCount,
            long stockoutCancelledCount) {
        this.item = item;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.restockRequestedCount = restockRequestedCount;
        this.stockoutCancelledCount = stockoutCancelledCount;
    }
}
