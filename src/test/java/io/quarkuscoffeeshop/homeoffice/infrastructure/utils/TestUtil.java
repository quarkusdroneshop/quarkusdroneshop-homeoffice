package io.quarkuscoffeeshop.homeoffice.infrastructure.utils;

import io.quarkuscoffeeshop.homeoffice.domain.Order;
import io.quarkuscoffeeshop.homeoffice.infrastructure.OrderMocker;

public class TestUtil {

    public static Order stubOrder() {

        OrderMocker orderMocker = new OrderMocker();
        return orderMocker.mockOrder();

    }

}
