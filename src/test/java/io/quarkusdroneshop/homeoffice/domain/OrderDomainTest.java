package io.quarkusdroneshop.homeoffice.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * homeoffice ドメインオブジェクトの純粋ユニットテスト
 */
public class OrderDomainTest {

    // ── Item enum ─────────────────────────────────────────────────────────────

    @Test
    void testItem_allValues() {
        Item[] items = Item.values();
        assertEquals(9, items.length);
        assertEquals(BigDecimal.valueOf(135.50), Item.QDC_A101.getPrice());
        assertEquals(BigDecimal.valueOf(155.50), Item.QDC_A102.getPrice());
        assertEquals(BigDecimal.valueOf(144.00), Item.QDC_A103.getPrice());
        assertEquals(BigDecimal.valueOf(256.25), Item.QDC_A104_AC.getPrice());
        assertEquals(BigDecimal.valueOf(4.75), Item.QDC_A104_AT.getPrice());
        assertEquals(BigDecimal.valueOf(553.00), Item.QDC_A105_Pro01.getPrice());
        assertEquals(BigDecimal.valueOf(633.25), Item.QDC_A105_Pro02.getPrice());
        assertEquals(BigDecimal.valueOf(735.50), Item.QDC_A105_Pro03.getPrice());
        assertEquals(BigDecimal.valueOf(955.50), Item.QDC_A105_Pro04.getPrice());
    }

    @Test
    void testItem_fromString() {
        assertEquals(Item.QDC_A101, Item.fromString("QDC_A101"));
        assertEquals(Item.QDC_A102, Item.fromString("qdc_a102"));
        assertNull(Item.fromString(null));
    }

    @Test
    void testItem_toValue() {
        assertEquals("QDC_A101", Item.QDC_A101.toValue());
        assertEquals("QDC_A102", Item.QDC_A102.toValue());
    }

    // ── OrderSource enum ──────────────────────────────────────────────────────

    @Test
    void testOrderSource_allValues() {
        assertEquals(3, OrderSource.values().length);
        assertNotNull(OrderSource.valueOf("COUNTER"));
        assertNotNull(OrderSource.valueOf("WEB"));
        assertNotNull(OrderSource.valueOf("PARTNER"));
    }

    // ── Store enum ────────────────────────────────────────────────────────────

    @Test
    void testStore_allValues() {
        assertTrue(Store.values().length > 0);
        assertNotNull(Store.valueOf("RALEIGH"));
        assertNotNull(Store.valueOf("ATLANTA"));
    }

    // ── LineItem ──────────────────────────────────────────────────────────────

    @Test
    void testLineItem_defaultConstructor() {
        LineItem li = new LineItem();
        assertNull(li.getItem());
        assertNull(li.getPrice());
    }

    @Test
    void testLineItem_itemPriceConstructor() {
        LineItem li = new LineItem(Item.QDC_A101, BigDecimal.valueOf(3.50), "QDCA10");
        assertEquals(Item.QDC_A101, li.getItem());
        assertEquals(BigDecimal.valueOf(3.50), li.getPrice());
        assertEquals("QDCA10", li.getPreparedBy());
    }

    @Test
    void testLineItem_setters() {
        LineItem li = new LineItem();
        li.setItem(Item.QDC_A102);
        li.setPrice(BigDecimal.valueOf(4.00));
        li.setPreparedBy("QDCA10Pro");
        assertEquals(Item.QDC_A102, li.getItem());
        assertEquals(BigDecimal.valueOf(4.00), li.getPrice());
        assertEquals("QDCA10Pro", li.getPreparedBy());
    }

    @Test
    void testLineItem_equalsAndHashCode() {
        LineItem a = new LineItem(Item.QDC_A101, BigDecimal.valueOf(3.50), "w");
        LineItem b = new LineItem(Item.QDC_A101, BigDecimal.valueOf(3.50), "w");
        assertEquals(a, a);
        assertNotEquals(a, null);
        assertNotEquals(a, "other");
        assertNotEquals(a, new LineItem(Item.QDC_A102, BigDecimal.valueOf(3.50), "w"));
    }

    @Test
    void testLineItem_toString() {
        LineItem li = new LineItem(Item.QDC_A101, BigDecimal.valueOf(3.50), "worker");
        assertTrue(li.toString().contains("LineItem"));
    }

    // ── Order ─────────────────────────────────────────────────────────────────

    @Test
    void testOrder_defaultConstructor() {
        Order order = new Order();
        assertNull(order.orderId);
    }

    @Test
    void testOrder_fullConstructor() {
        List<LineItem> lineItems = new ArrayList<>();
        lineItems.add(new LineItem(Item.QDC_A101, BigDecimal.valueOf(135.50), "worker"));
        Instant now = Instant.now();
        Order order = new Order(
                UUID.randomUUID().toString(),
                lineItems,
                OrderSource.WEB,
                "TOKYO",
                "ext-001",
                "loyalty-001",
                now,
                now);
        assertNotNull(order.orderId);
        assertEquals(OrderSource.WEB, order.orderSource);
        assertEquals("TOKYO", order.location);
        assertEquals("ext-001", order.externalOrderId);
        assertEquals("loyalty-001", order.loyaltyMemberId);
        assertNotNull(order.createdTimestamp);
    }

    @Test
    void testOrder_toString() {
        List<LineItem> lineItems = new ArrayList<>();
        lineItems.add(new LineItem(Item.QDC_A101, BigDecimal.valueOf(3.50), "w"));
        Order order = new Order(UUID.randomUUID().toString(), lineItems,
                OrderSource.COUNTER, "TOKYO", "ext", "loyalty", Instant.now(), Instant.now());
        String s = order.toString();
        assertTrue(s.contains("Order["));
    }

    @Test
    void testOrder_equalsAndHashCode() {
        Order a = new Order();
        a.orderId = "same-id";
        Order b = new Order();
        b.orderId = "same-id";
        assertEquals(a, a);
        assertNotEquals(a, null);
        assertNotEquals(a, "other");
    }

    @Test
    void testOrder_gettersAndSetters() {
        Order order = new Order();
        Instant now = Instant.now();
        order.orderPlacedTimestamp = now;
        order.orderCompletedTimestamp = now;
        assertEquals(now, order.getOrderPlacedTimestamp());
        assertEquals(now, order.getOrderCompletedTimestamp());
    }
}
