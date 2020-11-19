package io.quarkuscoffeeshop.homeoffice.viewmodels;

import io.quarkuscoffeeshop.homeoffice.domain.Order;

import java.util.List;

public class LocationOrders {
    public String location;
    public List<Order> orders;

    public LocationOrders(String location, List<Order> orders){
        this.location = location;
        this.orders = orders;
    }
}
