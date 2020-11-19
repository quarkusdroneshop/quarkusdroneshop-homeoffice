package io.quarkuscoffeeshop.homeoffice.infrastructure;

import graphql.schema.idl.SchemaParser;
import io.quarkuscoffeeshop.homeoffice.domain.*;
import io.quarkuscoffeeshop.homeoffice.viewmodels.*;
import org.antlr.v4.runtime.misc.Pair;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

@GraphQLApi
public class OrdersResource {

    Logger logger = LoggerFactory.getLogger(OrdersResource.class);

    @Inject
    OrderService orderService;

    //@Query("orders")
    @Query
    @Description("Get all orders from all stores")
    public List<Order> getOrders() {
        return Order.listAll();
    }


    @Query
    @Description("Get all orders from store by locationId")
    public List<Order> getOrdersForLocation(String locationId) {
        return Order.list("locationId", locationId);
    }

    @Query
    public List<LocationOrders> getOrdersByLocation() {
        List<LocationOrders> aggregate = new ArrayList<>();
        for (StoreLocation location : StoreLocation.values()) {
            List<Order> locationOrders =  Order.list("locationId", location.name());
            aggregate.add(new LocationOrders(location.name(), locationOrders));
        }
        return aggregate;
    }

    @Query
    public List<ItemSales> getItemSales(){
        List<ItemSales> sales = new ArrayList<>();

        for (Item item : Item.values()) {
            long soldItems = LineItem.count("item", item);
            ItemSales itemSales = new ItemSales();
            itemSales.item = item;
            itemSales.sales = soldItems;
            itemSales.revenue = item.getPrice().multiply(BigDecimal.valueOf(itemSales.sales));

            sales.add(itemSales);
        }
        return sales;
    }


    //example gql query
    /*
    query {
      storeServerSales {
        server
        store,
        sales{
          item,
          sales,
          revenue
        }
      }
    }
     */
    @Query
    public List<StoreServerSales> getStoreServerSales(){
        //I have to come document this - a lot of Hashtable work to get a count of unique items sold by servers by location
        List<StoreServerSales> storeServerSalesList = new ArrayList<>();

        for (StoreLocation location : StoreLocation.values()) {

            Hashtable servers = new Hashtable();


            //get an array of all lineItems for the location
            //this is so much easier using LINQ with entity framework in C#
            List<LineItem> locationLineItems = new ArrayList<>();
            List<Order> orders = Order.list("locationId", location.name());
            for( Order order : orders){
                locationLineItems.addAll(order.getLineItems());
            }

            //logger.debug("Location: {} : lineItems {}", location.name(), locationLineItems.size() );

            for (LineItem lineItem : locationLineItems){
               if (servers.containsKey(lineItem.getPreparedBy())){
                   //logger.debug("servers contains key: {}",lineItem.getPreparedBy());

                   Hashtable items = (Hashtable) servers.get(lineItem.getPreparedBy());

                   if (items.containsKey(lineItem.getItem())){
                       //update
                       ItemSales itemSales = (ItemSales) items.get(lineItem.getItem());
                       itemSales.sales  = itemSales.sales + 1;
                       itemSales.revenue = itemSales.revenue.add(lineItem.getPrice());
                       items.put(lineItem.getItem(), itemSales);

                   }else{
                       //new
                       ItemSales itemSales = new ItemSales(lineItem.getItem(), 1, lineItem.getPrice());
                       items.put(lineItem.getItem(), itemSales);
                   }
                   servers.put(lineItem.getPreparedBy(),items);

               }else{
                   Hashtable items = new Hashtable();
                   ItemSales itemSales = new ItemSales(lineItem.getItem(), 1, lineItem.getPrice());
                   items.put(lineItem.getItem(), itemSales);

                   //logger.debug("Adding to core - item: {}, array: {}",lineItem.getPreparedBy(), items.size());
                   servers.put(lineItem.getPreparedBy(), items);
               }
            }

            servers.forEach((key, value)->{
                String server = (String) key;
                Hashtable itemSalesHashTable = (Hashtable) servers.get(key);

                StoreServerSales sales = new StoreServerSales();
                sales.store = location.name();
                sales.server = server;

                List<ItemSales> itemSales = new ArrayList<>();
                itemSalesHashTable.forEach((k, v)->{
                    itemSales.add((ItemSales) v);
                });
                sales.sales = itemSales;

                storeServerSalesList.add(sales);
            });

        }

        //logger.debug("stores: " + storeServerSalesList.size());
        return storeServerSalesList;
    }
}