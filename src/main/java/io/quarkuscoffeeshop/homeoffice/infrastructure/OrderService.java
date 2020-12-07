package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkuscoffeeshop.homeoffice.domain.Item;
import io.quarkuscoffeeshop.homeoffice.domain.LineItem;
import io.quarkuscoffeeshop.homeoffice.domain.Order;
import io.quarkuscoffeeshop.homeoffice.domain.StoreLocation;
import io.quarkuscoffeeshop.homeoffice.domain.view.LineItemSalesReport;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrderService {

    public List<Order> allOrders() {
        return Order.listAll();
    }

    public List<Order> allOrdersByLocation(final StoreLocation storeLocation) {
        return Order.find("locationId = :locationId", new HashMap<String, Object>(){{ put("locationId", storeLocation); }}).list();
    }

    public List<LineItem> getLineItemSales() {
        List<LineItem> lineItemList = LineItem.listAll();
        Map<Item, List<LineItem>> lineItemMap =
                lineItemList.stream().collect(Collectors.groupingBy(LineItem::getItem));
        return null;
    }
}
