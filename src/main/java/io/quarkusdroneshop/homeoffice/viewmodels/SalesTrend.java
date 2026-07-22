package io.quarkusdroneshop.homeoffice.viewmodels;

public class SalesTrend {

    public String item;
    public long windowStart;
    public long windowEnd;
    public long orderCount;
    public double revenue;
    public String assemblyLine;
    public String location;

    public SalesTrend() {
    }

    public SalesTrend(String item, long windowStart, long windowEnd, long orderCount, double revenue,
            String assemblyLine, String location) {
        this.item = item;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.orderCount = orderCount;
        this.revenue = revenue;
        this.assemblyLine = assemblyLine;
        this.location = location;
    }
}
