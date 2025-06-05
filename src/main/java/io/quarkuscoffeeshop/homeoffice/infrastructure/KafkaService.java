package io.quarkuscoffeeshop.homeoffice.infrastructure;

import io.quarkuscoffeeshop.homeoffice.domain.LineItem;
import io.quarkuscoffeeshop.homeoffice.domain.Order;
import io.quarkuscoffeeshop.homeoffice.infrastructure.domain.OrderRecord;
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
    public void onOrderCreated(final OrderRecord orderRecord) {

        LOGGER.debug("IngressOrder received: {}", orderRecord);
        Order order = convertOrdeRecordToOrder(orderRecord);
        LOGGER.debug("Order : {}", order);
        //test
        orderRepository.persist(order);
    }

    protected Order convertOrdeRecordToOrder(final OrderRecord orderRecord) {

        List<LineItem> lineItems = new ArrayList<>();
        if (!(orderRecord.baristaLineItems()==null)) {
            orderRecord.baristaLineItems().forEach(l -> {
                lineItems.add(new LineItem(l.item(), BigDecimal.valueOf(3.00), l.name()));
            });
        }
        if (!(orderRecord.kitchenLineItems()==null)) {
            orderRecord.kitchenLineItems().forEach(k -> {
                lineItems.add(new LineItem(k.item(), BigDecimal.valueOf(3.50), k.name()));
            });
        }
        return new Order(
                orderRecord.orderId(),
                lineItems,
                orderRecord.orderSource(),
                "ATLANTA",
                orderRecord.loyaltyMemberId() == null ? null : orderRecord.loyaltyMemberId(),
                orderRecord.timestamp(),
                Instant.now()
        );
    }

    @Incoming(ORDERS_UPDATED)
    @Blocking
    @Transactional
    public void onOrderUpated(final OrderRecord orderRecord) {

        LOGGER.debug("OrderRecord received: {}", orderRecord);
        Order order = convertOrdeRecordToOrder(orderRecord);
        LOGGER.debug("Order : {}", order);
    }

    @Incoming(LOYALTY_MEMBER_PURCHASE)
    @Blocking
    @Transactional
    public void onLoyaltyMemberPurchase(final OrderRecord orderRecord) {

        LOGGER.debug("IngressOrder received: {}", orderRecord);
        Order order = convertOrdeRecordToOrder(orderRecord);
        LOGGER.debug("Order : {}", order);
    }
}
