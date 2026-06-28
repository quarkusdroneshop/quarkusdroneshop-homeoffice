package io.quarkusdroneshop.homeoffice.viewmodels;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.OrderRecord;
import io.quarkusdroneshop.homeoffice.HomeOfficeInitializer;
import io.quarkusdroneshop.homeoffice.domain.Order;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name="averageorderuptime")
public class AverageOrderUpTime extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq_gen")
    @SequenceGenerator(name = "id_seq_gen", allocationSize = 1)
    public Long id;
    
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
    
        long newUpTimeSeconds = Duration.between(placed, completed).getSeconds();
        AverageOrderUpTime current = AverageOrderUpTime.find("FROM AverageOrderUpTime ORDER BY calculatedAt DESC").firstResult();

        if (current == null) {
            current = new AverageOrderUpTime();
            //current.averageTime = Math.min(300, (int) newUpTimeSeconds);
            current.orderCount = 1;
        } else {
            int totalTime = current.averageTime * current.orderCount;
            totalTime += newUpTimeSeconds;
            current.orderCount += 1;
            //current.averageTime = Math.min(300, totalTime / current.orderCount);
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
