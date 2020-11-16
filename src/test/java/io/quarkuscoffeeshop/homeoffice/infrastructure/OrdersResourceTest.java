package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class OrdersResourceTest {

    @Test
    public void testAllOrdersEndpoint() {
        given()
          .when()
            .get("/graphql-ui/")
          .then()
             .statusCode(200);
    }

}