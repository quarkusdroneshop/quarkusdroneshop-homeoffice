package io.quarkusdroneshop.homeoffice.infrastructure;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CorsRouteFilter {

    @RouteFilter(Integer.MAX_VALUE)
    void corsFilter(RoutingContext rc) {
        String origin = rc.request().getHeader("Origin");
        if (origin != null) {
            rc.response()
                .putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS")
                .putHeader("Access-Control-Allow-Headers", "Content-Type,Authorization")
                .putHeader("Access-Control-Allow-Credentials", "false");
        }

        if ("OPTIONS".equalsIgnoreCase(rc.request().method().name())) {
            rc.response().setStatusCode(200).end();
            return;
        }

        rc.next();
    }
}
