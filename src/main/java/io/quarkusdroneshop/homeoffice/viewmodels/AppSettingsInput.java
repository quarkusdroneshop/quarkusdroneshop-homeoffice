package io.quarkusdroneshop.homeoffice.viewmodels;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.graphql.Input;

@Input("AppSettingsInput")
@RegisterForReflection
public class AppSettingsInput {
    public String clusterDomains;
    public String serviceCluster;

    public String getClusterDomains() { return clusterDomains; }
    public String getServiceCluster() { return serviceCluster; }
    public void setClusterDomains(String v) { this.clusterDomains = v; }
    public void setServiceCluster(String v) { this.serviceCluster = v; }
}
