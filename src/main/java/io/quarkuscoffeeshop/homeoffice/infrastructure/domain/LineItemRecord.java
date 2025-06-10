package io.quarkuscoffeeshop.homeoffice.infrastructure.domain;

import io.quarkuscoffeeshop.homeoffice.domain.Item;

public class LineItemRecord {

    private Item item;
    private double price;
    private String preparedBy;
    private String id;

    public LineItemRecord() {
    }

    public Item getItem() { return item; }
    public double getPrice() { return price; }
    public String getId() { return id; }
    public String getPreparedBy() {
        return preparedBy;
    }

    @Override
    public String toString() {
        return "LineItem[item=" + item + ", price=" + price + ", preparedBy=" + preparedBy + "]";
    }
}