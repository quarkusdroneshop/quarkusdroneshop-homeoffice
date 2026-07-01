package io.quarkusdroneshop.homeoffice.viewmodels;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.graphql.Input;

@Input("ServiceHealthInput")
@RegisterForReflection
public class ServiceHealthInput {
    public String name;
    public String url;

    public ServiceHealthInput() {}

    public String getName() { return name; }
    public String getUrl()  { return url; }
    public void setName(String name) { this.name = name; }
    public void setUrl(String url)   { this.url  = url; }
}
