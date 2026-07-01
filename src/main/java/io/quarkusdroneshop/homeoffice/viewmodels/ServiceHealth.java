package io.quarkusdroneshop.homeoffice.viewmodels;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ServiceHealth {
    public String name;
    public String status;  // "UP", "DOWN", "UNKNOWN"
    public String detail;

    public ServiceHealth() {}

    public ServiceHealth(String name, String status, String detail) {
        this.name   = name;
        this.status = status;
        this.detail = detail;
    }

    public String getName()   { return name; }
    public String getStatus() { return status; }
    public String getDetail() { return detail; }
}
