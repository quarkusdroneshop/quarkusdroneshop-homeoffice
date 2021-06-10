package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkuscoffeeshop.homeoffice.domain.LineItem;
import io.quarkuscoffeeshop.homeoffice.domain.Order;
import io.quarkuscoffeeshop.homeoffice.domain.OrderSource;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.IngressOrder;
import io.smallrye.reactive.messaging.annotations.Blocking;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static io.quarkuscoffeeshop.homeoffice.infrastructure.KafkaTopics.*;

@ApplicationScoped
public class KafkaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaService.class);

    @Incoming(ORDERS_CREATED)
    @Blocking
    @Transactional
    public void onOrderCreated(final IngressOrder ingressOrder) {

        LOGGER.debug("IngressOrder received: {}", ingressOrder);
        Order order = convertIngressOrderToOrder(ingressOrder);
        LOGGER.debug("Order : {}", order);
    }

    protected Order convertIngressOrderToOrder(final IngressOrder ingressOrder) {

        List<LineItem> lineItems = new ArrayList<>();
        if (ingressOrder.getBaristaLineItems().isPresent()) {
            ingressOrder.getBaristaLineItems().get().forEach(l -> {
                lineItems.add(new LineItem(l.getItem(), BigDecimal.valueOf(3.00), l.getName()));
            });
        }
        if (ingressOrder.getKitchenLineItems().isPresent()) {
            ingressOrder.getKitchenLineItems().get().forEach(k -> {
                lineItems.add(new LineItem(k.getItem(), BigDecimal.valueOf(3.50), k.getName()));
            });
        }
        return new Order(
                ingressOrder.getOrderId(),
                lineItems,
                ingressOrder.getOrderSource(),
                "ATLANTA",
                ingressOrder.getLoyaltyMemberId().isPresent() ? ingressOrder.getLoyaltyMemberId().get() : null,
                ingressOrder.getTimestamp(),
                Instant.now()
        );

    }

    @Incoming(ORDERS_UPDATED)
    @Blocking
    @Transactional
    public void onOrderUpated(final IngressOrder ingressOrder) {

        LOGGER.debug("IngressOrder received: {}", ingressOrder);
        Order order = convertIngressOrderToOrder(ingressOrder);
        LOGGER.debug("Order : {}", order);
    }

    @Incoming(LOYALTY_MEMBER_PURCHASE)
    @Blocking
    @Transactional
    public void onLoyaltyMemberPurchase(final IngressOrder ingressOrder) {

        LOGGER.debug("IngressOrder received: {}", ingressOrder);
        Order order = convertIngressOrderToOrder(ingressOrder);
        LOGGER.debug("Order : {}", order);
    }
}
