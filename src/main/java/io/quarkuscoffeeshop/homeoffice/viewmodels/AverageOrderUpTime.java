package io.quarkuscoffeeshop.homeoffice.viewmodels;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.OrderRecord;
import io.quarkuscoffeeshop.homeoffice.HomeOfficeInitializer;
import io.quarkuscoffeeshop.homeoffice.domain.Order;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.Instant;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name="AverageOrderUpTime")
public class AverageOrderUpTime extends PanacheEntity {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AverageOrderUpTime.class);

    public int averageTime;
    public int orderCount;
    public Instant calculatedAt;

    public static AverageOrderUpTime fromOrderRecord(Order order) {
        Instant placed = order.getOrderPlacedTimestamp();
        Instant completed = order.getOrderCompletedTimestamp();
    
        if (placed == null || completed == null) {
            LOGGER.warn("Start or end time is null. Skipping update for record: {}", order);
            return null;
        }
    
        double newUpTimeSeconds = Duration.between(placed, completed).getSeconds();
        AverageOrderUpTime current = AverageOrderUpTime.find("FROM AverageOrderUpTime ORDER BY calculatedAt DESC").firstResult();
        
        if (current == null) {
            current = new AverageOrderUpTime();
            current.averageTime = (int) newUpTimeSeconds;
            current.orderCount = 1;
            current.calculatedAt = Instant.now();
        } else {
            int totalTime = current.averageTime * current.orderCount;
            totalTime += newUpTimeSeconds;
            current.orderCount += 1;
            current.averageTime = totalTime / current.orderCount;
        }
        current.calculatedAt = Instant.now();
        current.persist();
        return current;
    }

    public static AverageOrderUpTime fromOrder(Order order) {
        if (order == null || order.orderCompletedTimestamp == null || order.orderPlacedTimestamp == null) {
            return null;
        }
    
        Duration duration = Duration.between(order.orderPlacedTimestamp, order.orderCompletedTimestamp);
    
        AverageOrderUpTime averageOrderUpTime = new AverageOrderUpTime();
        return averageOrderUpTime;
    }
}
