package io.quarkusdroneshop.homeoffice.viewmodels;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class AppSettingsResult {
    /** JSON string: {"a-cluster":"...", "b-cluster":"...", "c-cluster":"..."} */
    public String clusterDomains;
    /** JSON string: {"Web":"b-cluster", "Counter":"b-cluster", ...} */
    public String serviceCluster;

    public AppSettingsResult() {}

    public AppSettingsResult(String clusterDomains, String serviceCluster) {
        this.clusterDomains = clusterDomains;
        this.serviceCluster = serviceCluster;
    }

    public String getClusterDomains() { return clusterDomains; }
    public String getServiceCluster() { return serviceCluster; }
}
