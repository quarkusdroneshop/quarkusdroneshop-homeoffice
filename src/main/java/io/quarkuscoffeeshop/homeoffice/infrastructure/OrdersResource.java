package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkuscoffeeshop.homeoffice.domain.Order;
import io.quarkuscoffeeshop.homeoffice.domain.StoreLocation;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

@GraphQLApi
public class OrdersResource {

    Logger logger = LoggerFactory.getLogger(OrdersResource.class);

    @Inject
    OrderService orderService;

    @Query("allOrders")
    @Description("Get all orders from all stores")
    public List<Order> allOrders() {

        return orderService.allOrders();
    }

    /**
     * query{
     *   allOrdersByLocation(locationId: ATLANTA){
     *     id
     *   }
     * }
     * @param storeLocation
     * @return
     */
    @Query("allOrdersByLocation")
    @Description("Get all orders from a single location")
    public List<Order> allOrdersByLocation(@Name("locationId") StoreLocation storeLocation) {
        return orderService.allOrdersByLocation(storeLocation);
    }


}