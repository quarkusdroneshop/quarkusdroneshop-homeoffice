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
    

    public static void updateFromOrderRecord(OrderRecord orderRecord) {
        
        if (orderRecord.getStartTime() == null || orderRecord.getEndTime() == null) {
            LOGGER.warn("Start or end time is null. Skipping update for record: {}", orderRecord);
            return;
        }

        long newUpTimeSeconds = Duration.between(orderRecord.timeIn, orderRecord.timeUp).getSeconds();

        AverageOrderUpTime current = AverageOrderUpTime.find("order by calculatedAt desc").firstResult();

        if (current == null) {
            current = new AverageOrderUpTime();
            current.averageTime = (int)newUpTimeSeconds;
            current.orderCount = 1;
        } else {
            int totalTime = current.averageTime * current.orderCount;
            totalTime += newUpTimeSeconds;
            current.orderCount += 1;
            current.averageTime = totalTime / current.orderCount;
        }

        current.calculatedAt = Instant.now();
        current.persist();
    }
}
