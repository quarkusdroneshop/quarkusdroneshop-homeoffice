package io.quarkusdroneshop.homeoffice.infrastructure;

import io.quarkusdroneshop.homeoffice.domain.LineItem;
import io.quarkusdroneshop.homeoffice.domain.Order;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.OrderRecord;
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

import static io.quarkusdroneshop.homeoffice.infrastructure.KafkaTopics.*;

@ApplicationScoped
public class KafkaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaService.class);

    @Inject
    OrderService orderService;

    @Incoming(ORDERS_CREATED)
    @Blocking
    public void onOrderCreated(final OrderRecord orderRecord) {

        LOGGER.debug("IngressOrder received: {}", orderRecord);
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
        orderService.process(orderRecord);
    }

    @Incoming(LOYALTY_MEMBER_PURCHASE)
    @Blocking
    @Transactional
    public void onLoyaltyMemberPurchase(final OrderRecord orderRecord) {

        LOGGER.debug("IngressOrder received: {}", orderRecord);
        //Order order = convertOrderRecordToOrder(orderRecord);
        //LOGGER.debug("Order : {}", order);
        //order.persist();
        orderService.process(orderRecord);
    }
}
