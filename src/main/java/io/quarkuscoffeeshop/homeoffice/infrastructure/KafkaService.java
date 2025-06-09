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

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static io.quarkuscoffeeshop.homeoffice.infrastructure.KafkaTopics.*;

@ApplicationScoped
public class KafkaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaService.class);

    @Inject
    OrderService orderService;

    @Incoming(ORDERS_CREATED)
    @Blocking
    public void onOrderCreated(final OrderRecord orderRecord) {

        LOGGER.debug("IngressOrder received: {}", orderRecord);
        //orderRecord.setExternalOrderId(orderRecord.orderId());
        //Order order = convertOrderRecordToOrder(orderRecord);
        //LOGGER.debug("Order : {}", order);
        //order.persist(); 
        orderService.process(orderRecord);
    }

    @Incoming(ORDERS_UPDATED)
    @Blocking
    @Transactional
    public void onOrderUpated(final OrderRecord orderRecord) {

        LOGGER.debug("OrderRecord received: {}", orderRecord);
        //Order order = convertOrderRecordToOrder(orderRecord);
        //LOGGER.debug("Order : {}", order);
        //order.persist();
    }

    @Incoming(LOYALTY_MEMBER_PURCHASE)
    @Blocking
    @Transactional
    public void onLoyaltyMemberPurchase(final OrderRecord orderRecord) {

        LOGGER.debug("IngressOrder received: {}", orderRecord);
        //Order order = convertOrderRecordToOrder(orderRecord);
        //LOGGER.debug("Order : {}", order);
        //order.persist();
    }
}
