package io.quarkusdroneshop.homeoffice.viewmodels;

public class StockoutRisk {

    public String item;
    public boolean atRisk;
    public long lastStockoutEventAt;

    public StockoutRisk() {
    }

    public StockoutRisk(String item, boolean atRisk, long lastStockoutEventAt) {
        this.item = item;
        this.atRisk = atRisk;
        this.lastStockoutEventAt = lastStockoutEventAt;
    }
}
