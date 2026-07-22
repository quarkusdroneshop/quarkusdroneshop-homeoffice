package io.quarkusdroneshop.homeoffice.infrastructure;

import io.quarkusdroneshop.homeoffice.domain.Item;
import io.quarkusdroneshop.homeoffice.domain.OrderSource;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.LineItemRecord;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.OrderRecord;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * dataproduct-order-events (明細単位でストリームされる ORDER_PLACED) を orderId ごとに
 * オンメモリで集約し、homeoffice の OrderService.process(OrderRecord) が期待する
 * 「注文全体 (qdca10LineItems + qdca10proLineItems まとめて)」の形へ組み立てる。
 *
 * dataproduct-order-events 自体には「この注文の明細はこれで全部」という区切りが無いため、
 * 一定時間 (DEBOUNCE) 新しい明細が届かなくなった注文を確定として扱う。
 */
@ApplicationScoped
public class OrderAssemblyAggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderAssemblyAggregator.class);

    // 同一注文の明細イベントはほぼ同時刻にまとめて届く想定のため、数秒の静穏期間で確定とみなす。
    private static final long DEBOUNCE_MS = 3000;

    private final Map<String, BufferedOrder> buffer = new ConcurrentHashMap<>();

    @Inject
    OrderService orderService;

    @Incoming("orders-created")
    public void onLineItem(final OrderPlacedLineItem event) {
        if (event == null || event.orderId == null) {
            return;
        }

        LOGGER.debug("orders-created ORDER_PLACED line item received: orderId={}, item={}", event.orderId, event.item);
        BufferedOrder bufferedOrder = buffer.computeIfAbsent(event.orderId, id -> new BufferedOrder());
        bufferedOrder.addLineItem(event);
    }

    @Scheduled(every = "1s")
    public void flush() {
        long now = System.currentTimeMillis();

        // ConcurrentHashMap は forEach (イテレーション) 中に同じマップを computeIfPresent 等で
        // 変更すると "Recursive update" 例外を投げうる。まず対象の orderId 一覧をスナップショット
        // してからループ外で個別に取り出し・削除する (イテレーションとミューテーションを分離)。
        List<String> candidateOrderIds = new ArrayList<>();
        buffer.forEach((orderId, bufferedOrder) -> {
            if (now - bufferedOrder.lastEventAtMillis() >= DEBOUNCE_MS) {
                candidateOrderIds.add(orderId);
            }
        });

        for (String orderId : candidateOrderIds) {
            BufferedOrder existing = buffer.get(orderId);
            if (existing == null || now - existing.lastEventAtMillis() < DEBOUNCE_MS) {
                // 候補抽出後に新しい明細が追加された、または既に他スレッドが処理済み。次回サイクルへ。
                continue;
            }
            // Map#remove(key, value) は「まだ existing と同じインスタンスが紐付いている場合のみ」
            // 削除するアトミックな条件付き削除。computeIfPresent は remapping 関数の戻り値
            // (= null) を返してしまい取り出した値を得られないため使わない。
            if (!buffer.remove(orderId, existing)) {
                continue;
            }

            try {
                OrderRecord orderRecord = existing.toOrderRecord(orderId);
                LOGGER.debug("Assembled OrderRecord from dataproduct-order-events: {}", orderRecord);
                orderService.process(orderRecord);
            } catch (Exception e) {
                LOGGER.error("Failed to process assembled order {}", orderId, e);
            }
        }
    }

    private static class BufferedOrder {
        private volatile long lastEventAtMillis = System.currentTimeMillis();
        private volatile String orderSource;
        private volatile String location;
        private volatile String loyaltyMemberId;
        private final List<LineItemRecord> qdca10Items = new ArrayList<>();
        private final List<LineItemRecord> qdca10proItems = new ArrayList<>();

        synchronized void addLineItem(OrderPlacedLineItem event) {
            this.lastEventAtMillis = System.currentTimeMillis();
            this.orderSource = event.orderSource;
            this.location = event.location;
            this.loyaltyMemberId = event.loyaltyMemberId;

            LineItemRecord lineItemRecord = new LineItemRecord();
            try {
                lineItemRecord.setItem(event.item != null ? Item.valueOf(event.item) : null);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Unknown item code '{}' in dataproduct-order-events, skipping line item", event.item);
                return;
            }
            lineItemRecord.setName(event.name);
            lineItemRecord.setPrice(event.price != null ? event.price.doubleValue() : 0.0);
            lineItemRecord.setId(event.itemId);

            if ("QDCA10PRO".equals(event.assemblyLine)) {
                qdca10proItems.add(lineItemRecord);
            } else {
                qdca10Items.add(lineItemRecord);
            }
        }

        long lastEventAtMillis() {
            return lastEventAtMillis;
        }

        synchronized OrderRecord toOrderRecord(String orderId) {
            OrderRecord orderRecord = new OrderRecord();
            orderRecord.setOrderId(orderId);
            orderRecord.setExternalOrderId(orderId);
            try {
                orderRecord.setOrderSource(orderSource != null ? OrderSource.valueOf(orderSource) : null);
            } catch (IllegalArgumentException e) {
                orderRecord.setOrderSource(null);
            }
            orderRecord.setLocation(location);
            orderRecord.setCustomerLoyaltyId(loyaltyMemberId);
            orderRecord.setQdca10LineItems(new ArrayList<>(qdca10Items));
            orderRecord.setQdca10proLineItems(new ArrayList<>(qdca10proItems));
            orderRecord.orderPlacedTimestamp = Instant.ofEpochMilli(lastEventAtMillis);
            return orderRecord;
        }
    }
}
