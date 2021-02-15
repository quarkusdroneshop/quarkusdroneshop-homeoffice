package io.quarkuscoffeeshop.homeoffice.viewmodels;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name="AverageOrderUpTime")
public class AverageOrderUpTime extends PanacheEntity {
    public int averageTime;
    public int orderCount;
    public Instant calculatedAt;

}
