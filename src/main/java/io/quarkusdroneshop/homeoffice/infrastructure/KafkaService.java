package io.quarkusdroneshop.homeoffice.infrastructure;

import io.quarkusdroneshop.homeoffice.domain.LineItem;
import io.quarkusdroneshop.homeoffice.domain.Order;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.OrderRecord;
import io.smallrye.reactive.messaging.annotations.Blocking;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import jakarta.inject.Inject;
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

    @Inject
    @Channel(QDCA10_RETRY_OUT)
    Emitter<RetryOrderTicket> qdca10RetryEmitter;

    @Inject
    @Channel(QDCA10PRO_RETRY_OUT)
    Emitter<RetryOrderTicket> qdca10proRetryEmitter;

    @Incoming(ORDERS_CREATED)
    @Blocking
    public void onOrderCreated(final OrderRecord orderRecord) {

        if (orderRecord == null) {
            LOGGER.warn("Skipping null OrderRecord (unparseable message)");
            return;
        }

        LOGGER.debug("IngressOrder received: {}", orderRecord);
        //Order order = convertOrderRecordToOrder(orderRecord);
        //LOGGER.debug("Order : {}", order);
        //order.persist(); 
        orderService.process(orderRecord);
    }

    /**
     * qdca10 / qdca10pro が発行する OrderUp（{orderId, lineItemId, item, name, timestamp, madeBy}）
     * を受信し、該当する LineItem の preparedBy を更新する。以前はここを OrderRecord
     * （counter からの新規注文通知と同じ型）で受けていたため、形状が合わず全フィールドが
     * null のまま新しいゴミ注文を作成し続けていた。
     */
    @Incoming(ORDERS_UPDATED)
    @Blocking
    @Transactional
    public void onOrderUpated(final OrderUpMessage orderUp) {

        LOGGER.debug("OrderUp received: orderId={}, lineItemId={}, madeBy={}",
            orderUp.orderId, orderUp.lineItemId, orderUp.madeBy);

        if (orderUp.orderId == null || orderUp.lineItemId == null) {
            LOGGER.warn("OrderUp message missing orderId/lineItemId, ignoring: {}", orderUp.orderId);
            return;
        }

        Order order = Order.find("orderId", orderUp.orderId).firstResult();
        if (order == null) {
            LOGGER.warn("OrderUp received for unknown orderId: {}", orderUp.orderId);
            return;
        }

        List<LineItem> lineItems = order.getLineItems() != null
            ? new ArrayList<>(order.getLineItems())
            : new ArrayList<>();

        boolean matched = false;
        for (LineItem lineItem : lineItems) {
            if (lineItem.id != null && lineItem.id.toString().equals(orderUp.lineItemId)) {
                lineItem.setPreparedBy(orderUp.madeBy != null ? orderUp.madeBy : "unknown");
                matched = true;
                break;
            }
        }

        if (!matched) {
            LOGGER.warn("OrderUp lineItemId {} not found on order {}", orderUp.lineItemId, orderUp.orderId);
            return;
        }

        boolean allPrepared = !lineItems.isEmpty()
            && lineItems.stream().allMatch(li -> li.getPreparedBy() != null && !li.getPreparedBy().isBlank());
        if (allPrepared) {
            order.orderCompletedTimestamp = Instant.now();
        }
        order.persist();
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

    /**
     * Support 画面の Retry ボタンから Kafka 経由で届いた再処理リクエスト。
     * 1. preparedBy をクリアして未着手状態に戻し、orderPlacedTimestamp を更新することで
     *    Order Board 上で再び「In Queue」として表示されるようにする。
     * 2. 未完了の LineItem ごとに、実際の注文処理パイプライン（qdca10 / qdca10pro の
     *    物理端末シミュレータ）が消費する qdca10-in / qdca10pro-in トピックへ本物の
     *    チケットを re-publish し、In Progress → Order Up へ本当に進めるようにする。
     */
    @Incoming(ORDER_RETRY_IN)
    @Blocking
    @Transactional
    public void onOrderRetryRequested(final String orderId) {
        LOGGER.info("Order retry requested via Kafka for orderId: {}", orderId);

        Order order = Order.find("orderId", orderId).firstResult();
        if (order == null) {
            LOGGER.warn("Order retry requested for unknown orderId: {}", orderId);
            return;
        }

        String displayName = (order.getCustomerLoyaltyId() != null && !order.getCustomerLoyaltyId().isBlank())
            ? order.getCustomerLoyaltyId()
            : order.getOrderId().substring(Math.max(0, order.getOrderId().length() - 8));

        if (order.getLineItems() != null) {
            order.getLineItems().forEach(lineItem -> {
                lineItem.setPreparedBy(null);
                resendTicket(order.getOrderId(), lineItem, displayName);
            });
        }
        order.orderCompletedTimestamp = null;
        order.orderPlacedTimestamp = Instant.now();
        order.persist();
    }

    private void resendTicket(String orderId, LineItem lineItem, String displayName) {
        String upstreamItem = lineItem.getItem().name();
        RetryOrderTicket ticket = new RetryOrderTicket(orderId, lineItem.id.toString(), upstreamItem, displayName);

        if (upstreamItem.contains("_Pro")) {
            LOGGER.info("Re-publishing retry ticket to qdca10pro-in: {}", ticket.item);
            qdca10proRetryEmitter.send(ticket);
        } else {
            LOGGER.info("Re-publishing retry ticket to qdca10-in: {}", ticket.item);
            qdca10RetryEmitter.send(ticket);
        }
    }
}
