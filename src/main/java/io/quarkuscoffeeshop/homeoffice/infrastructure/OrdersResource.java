package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkuscoffeeshop.homeoffice.domain.Film;
import io.quarkuscoffeeshop.homeoffice.domain.Order;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

@GraphQLApi
public class OrdersResource {

    Logger logger = LoggerFactory.getLogger(OrdersResource.class);

    @Inject
    GalaxyService service;

    @Inject
    OrderService orderService;

    @Query("allFilms")
    @Description("Get all Films from a galaxy far far away")
    public List<Film> hello() {
        return service.getAllFilms();
    }

    @Query("allOrders")
    @Description("Get all orders from all stores")
    public List<Order> allOrders() {

        return orderService.allOrders();
    }
}