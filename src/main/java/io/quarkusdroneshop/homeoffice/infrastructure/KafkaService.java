package io.quarkusdroneshop.homeoffice.infrastructure;

import io.quarkusdroneshop.homeoffice.domain.LineItem;
import io.quarkusdroneshop.homeoffice.domain.Order;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.OrderRecord;
import io.smallrye.reactive.messaging.annotations.Blocking;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import jakarta.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.quarkusdroneshop.homeoffice.infrastructure.KafkaTopics.*;

@ApplicationScoped
public class KafkaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaService.class);

    @Inject
    OrderService orderService;

    @Inject
    @Channel(QDCA10_RETRY_OUT)
    Emitter<GenericRecord> qdca10RetryEmitter;

    @Inject
    @Channel(QDCA10PRO_RETRY_OUT)
    Emitter<GenericRecord> qdca10proRetryEmitter;

    // dataproduct-order-events (order-events Flink job の出力) と同じ Avro スキーマで
    // ORDER_PLACED イベントを組み立てて再送する。qdca10/qdca10pro は本番プロファイルでは
    // orders-in ではなくこのトピック (Avro, avro-confluent) しか購読していないため、
    // 以前のように JSON (RetryOrderTicket) を旧トピック (qdca10-in, dev専用) へ送っても
    // 誰にも消費されず Retry が事実上何もしていなかった。
    private static final Schema ORDER_EVENT_SCHEMA = loadOrderEventSchema();
    private static final Schema LINE_ITEM_SCHEMA = unwrapNullable(ORDER_EVENT_SCHEMA.getField("lineItem").schema());

    private static Schema loadOrderEventSchema() {
        try (InputStream is = KafkaService.class.getResourceAsStream("/avro/orders-event.avsc")) {
            return new Schema.Parser().parse(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load orders-event.avsc", e);
        }
    }

    private static Schema unwrapNullable(Schema schema) {
        if (schema.getType() != Schema.Type.UNION) return schema;
        return schema.getTypes().stream()
            .filter(s -> s.getType() != Schema.Type.NULL)
            .findFirst()
            .orElseThrow();
    }

    // orders-created (dataproduct-order-events) の取り込みは OrderAssemblyAggregator に
    // 移管した。明細単位で届くイベントを orderId ごとに集約してから OrderService.process() を
    // 呼ぶ必要があるため、単純な @Incoming ハンドラでは表現できずクラスを分けている。

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

        // dataproduct-order-events を共有購読しているため、LINE_ITEM_STATUS_CHANGED 以外
        // (ORDER_PLACED 等) は OrderUpMessageAvroDeserializer が null を返して破棄する。
        // 専用トピック (旧 shop-bsite.orders-up) の頃は全メッセージが必ず OrderUp だったため
        // このチェックが無く、null 混入で NullPointerException によりクラッシュループしていた。
        if (orderUp == null) {
            return;
        }

        LOGGER.debug("OrderUp received: orderId={}, lineItemId={}, madeBy={}",
            orderUp.orderId, orderUp.lineItemId, orderUp.madeBy);

        if (orderUp.orderId == null || orderUp.lineItemId == null) {
            LOGGER.warn("OrderUp message missing orderId/lineItemId, ignoring: {}", orderUp.orderId);
            return;
        }

        try {
            // 過去 (Item enum のリネーム前, 例: QDC_A105_PRO03 -> QDC_A105_Pro03) に保存された
            // 注文は、DB 上の item カラムが現在の enum 定数と一致せず Order.find() の時点で
            // IllegalArgumentException ("No enum constant ...") が飛ぶことがある。1件の不正
            // レコードでチャンネル全体がクラッシュループしないよう、ここで捕捉して読み飛ばす。
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
        } catch (Exception e) {
            LOGGER.error("Failed to process OrderUp for orderId={}, skipping", orderUp.orderId, e);
        }
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
        String assemblyLine = upstreamItem.contains("_Pro") ? "QDCA10PRO" : "QDCA10";

        GenericRecord lineItemRecord = new GenericData.Record(LINE_ITEM_SCHEMA);
        lineItemRecord.put("itemId", lineItem.id.toString());
        lineItemRecord.put("item", upstreamItem);
        lineItemRecord.put("name", displayName);
        BigDecimal price = lineItem.getPrice() != null ? lineItem.getPrice() : BigDecimal.ZERO;
        lineItemRecord.put("price", decimalToBytes(price.setScale(2, java.math.RoundingMode.HALF_UP)));
        lineItemRecord.put("lineItemStatus", "PLACED");
        lineItemRecord.put("assemblyLine", assemblyLine);
        lineItemRecord.put("madeBy", null);

        GenericRecord orderEvent = new GenericData.Record(ORDER_EVENT_SCHEMA);
        orderEvent.put("eventId", UUID.randomUUID().toString());
        orderEvent.put("orderId", orderId);
        orderEvent.put("eventType", "ORDER_PLACED");
        orderEvent.put("eventTimestamp", Instant.now().toEpochMilli());
        orderEvent.put("orderSource", null);
        orderEvent.put("location", null);
        orderEvent.put("loyaltyMemberId", null);
        orderEvent.put("orderStatus", "PLACED");
        orderEvent.put("lineItem", lineItemRecord);
        orderEvent.put("sourceDomain", "homeoffice-retry");
        orderEvent.put("sourceTopic", "order-retry-in");

        if ("QDCA10PRO".equals(assemblyLine)) {
            LOGGER.info("Re-publishing retry ORDER_PLACED event to qdca10pro: {}", upstreamItem);
            qdca10proRetryEmitter.send(orderEvent);
        } else {
            LOGGER.info("Re-publishing retry ORDER_PLACED event to qdca10: {}", upstreamItem);
            qdca10RetryEmitter.send(orderEvent);
        }
    }

    private static ByteBuffer decimalToBytes(BigDecimal value) {
        return ByteBuffer.wrap(value.unscaledValue().toByteArray());
    }
}
