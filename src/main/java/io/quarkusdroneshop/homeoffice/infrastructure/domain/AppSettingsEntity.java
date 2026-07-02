package io.quarkusdroneshop.homeoffice.infrastructure.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Singleton settings row (id = 1).
 * cluster_domains and service_cluster are stored as JSON strings
 * and parsed/serialized in the GraphQL layer.
 */
@Entity
@Table(name = "app_settings")
@RegisterForReflection
public class AppSettingsEntity extends PanacheEntityBase {

    @Id
    public Long id = 1L;

    @Column(name = "cluster_domains", nullable = false)
    public String clusterDomains = "{}";

    @Column(name = "service_cluster", nullable = false)
    public String serviceCluster = "{}";

    public static AppSettingsEntity load() {
        AppSettingsEntity e = findById(1L);
        if (e == null) {
            e = new AppSettingsEntity();
            e.persist();
        }
        return e;
    }
}
