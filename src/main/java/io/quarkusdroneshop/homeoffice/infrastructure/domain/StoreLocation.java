package io.quarkusdroneshop.homeoffice.infrastructure.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Entity;
import java.util.Objects;

@Entity
public class StoreLocation extends PanacheEntity {

    String location;

    public StoreLocation() {
    }

    public StoreLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "StoreLocationEntity{" +
                "location='" + location + '\'' +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreLocation that = (StoreLocation) o;
        return Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location);
    }
}
