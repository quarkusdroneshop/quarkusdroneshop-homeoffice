package io.quarkusdroneshop.homeoffice.infrastructure.domain;

import io.quarkusdroneshop.homeoffice.domain.Item;

public class LineItemRecord {

    private Item item;
    private double price;
    private String name; // ← 追加
    private String preparedBy;
    private String id;

    public LineItemRecord() {}

    public Item getItem() { return item; }
    public double getPrice() { return price; }
    public String getName() { return name; } // ← 追加
    public String getId() { return id; }
    public String getPreparedBy() { return preparedBy; }

    public void setItem(Item item) { this.item = item; }
    public void setPrice(double price) { this.price = price; }
    public void setName(String name) { this.name = name; } // ← 追加
    public void setPreparedBy(String preparedBy) { this.preparedBy = preparedBy; }
    public void setId(String id) { this.id = id; }

    @Override
    public String toString() {
        return "LineItem[item=" + item + ", name=" + name + ", price=" + price + ", preparedBy=" + preparedBy + "]";
    }
}