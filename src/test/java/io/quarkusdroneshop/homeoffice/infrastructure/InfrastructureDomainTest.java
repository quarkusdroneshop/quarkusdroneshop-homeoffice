package io.quarkusdroneshop.homeoffice.infrastructure;

import io.quarkusdroneshop.homeoffice.domain.Item;
import io.quarkusdroneshop.homeoffice.domain.OrderSource;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.*;
import io.quarkusdroneshop.homeoffice.viewmodels.ItemSales;
import io.quarkusdroneshop.homeoffice.viewmodels.LocationOrders;
import io.quarkusdroneshop.homeoffice.viewmodels.ProductItemSales;
import io.quarkusdroneshop.homeoffice.viewmodels.ProductSales;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * インフラドメインオブジェクトの純粋ユニットテスト
 */
public class InfrastructureDomainTest {

    // ── LineItemRecord ────────────────────────────────────────────────────────

    @Test
    void testLineItemRecord_gettersAndSetters() {
        LineItemRecord rec = new LineItemRecord();
        rec.setItem(Item.QDC_A101);
        rec.setPrice(135.50);
        rec.setName("Test Item");
        rec.setPreparedBy("Worker");
        rec.setId("id-001");

        assertEquals(Item.QDC_A101, rec.getItem());
        assertEquals(135.50, rec.getPrice());
        assertEquals("Test Item", rec.getName());
        assertEquals("Worker", rec.getPreparedBy());
        assertEquals("id-001", rec.getId());
        assertTrue(rec.toString().contains("QDC_A101"));
    }

    // ── OrderRecord ───────────────────────────────────────────────────────────

    @Test
    void testOrderRecord_defaultConstructorAndGetters() {
        OrderRecord rec = new OrderRecord();
        assertNull(rec.orderId());
        assertNull(rec.getQdca10LineItems());
        assertNull(rec.getQdca10proLineItems());
        assertEquals(0.0, rec.total());
        assertNull(rec.orderSource());
        assertNull(rec.location());
        assertNull(rec.externalOrderId());
        assertNull(rec.customerLoyaltyId());
        assertNull(rec.orderPlacedTime());
        assertNull(rec.orderCompletedTime());
        assertNull(rec.timestamp());
    }

    @Test
    void testOrderRecord_setAndGet_publicFields() {
        OrderRecord rec = new OrderRecord();
        Instant now = Instant.now();
        rec.orderPlacedTimestamp = now;
        rec.orderCompletedTimestamp = now;
        rec.timestamp = now;
        assertEquals(now, rec.orderPlacedTimestamp());
        assertEquals(now, rec.orderCompletedTimestamp());
        assertEquals(now, rec.timestamp());
        assertNotNull(rec.toString());
        assertNotNull(rec.getExternalOrderId());
        assertNull(rec.getExternalOrderId()); // null
    }

    // ── EventType ─────────────────────────────────────────────────────────────

    @Test
    void testEventType_allValues() {
        assertTrue(EventType.values().length > 0);
        assertNotNull(EventType.valueOf("OrderCreated"));
        assertNotNull(EventType.valueOf("OrderUpdated"));
        assertNotNull(EventType.valueOf("LoyaltyMemberPurchase"));
    }

    // ── KafkaTopics ───────────────────────────────────────────────────────────

    @Test
    void testKafkaTopics_constants() {
        assertEquals("orders-created", KafkaTopics.ORDERS_CREATED);
        assertEquals("orders-updated", KafkaTopics.ORDERS_UPDATED);
        assertEquals("loyalty-member-purchase", KafkaTopics.LOYALTY_MEMBER_PURCHASE);
    }

    // ── ProductItemSales ──────────────────────────────────────────────────────

    @Test
    void testProductItemSales_constructors() {
        ProductItemSales s1 = new ProductItemSales();
        assertNull(s1.item);

        ProductItemSales s2 = new ProductItemSales(Item.QDC_A101,
                BigDecimal.valueOf(3), BigDecimal.valueOf(405.0), Instant.now());
        assertEquals(Item.QDC_A101, s2.item);
        assertEquals(BigDecimal.valueOf(3), s2.salesTotal);
        assertEquals(BigDecimal.valueOf(405.0), s2.revenue);
        assertNotNull(s2.saleDate);
    }

    @Test
    void testProductItemSales_setters() {
        ProductItemSales s = new ProductItemSales();
        s.setSalesTotal(BigDecimal.valueOf(5));
        s.setPreparedBy("Worker");
        assertEquals(BigDecimal.valueOf(5), s.salesTotal);
        assertEquals("Worker", s.preparedBy);
    }

    // ── ProductSales ──────────────────────────────────────────────────────────

    @Test
    void testProductSales_constructors() {
        ProductSales ps1 = new ProductSales();
        assertNull(ps1.item);

        ProductSales ps2 = new ProductSales(Item.QDC_A101);
        assertEquals(Item.QDC_A101, ps2.item);
        assertEquals(Item.QDC_A101, ps2.getItem());
        assertNotNull(ps2.productItemSales);
    }

    @Test
    void testProductSales_addProductItemSale() {
        ProductSales ps = new ProductSales(Item.QDC_A102);
        ProductItemSales itemSale = new ProductItemSales(Item.QDC_A102,
                BigDecimal.ONE, BigDecimal.valueOf(155.50), Instant.now());
        ps.addProductItemSale(itemSale);
        assertEquals(1, ps.productItemSales.size());
        assertEquals(ps, itemSale.productSales);
    }

    // ── ItemSales ─────────────────────────────────────────────────────────────

    @Test
    void testItemSales_constructors() {
        ItemSales s1 = new ItemSales();
        assertNull(s1.item);

        ItemSales s2 = new ItemSales(Item.QDC_A103, 2L, 288.0);
        assertEquals(Item.QDC_A103, s2.item);
        assertEquals(2L, s2.salesTotal);
        assertEquals(288.0, s2.revenue);

        ItemSales s3 = new ItemSales(Item.QDC_A104_AC, 1L, 256.25, Instant.now(), 256.25);
        assertEquals(Item.QDC_A104_AC, s3.item);
        assertEquals(256.25, s3.price);
    }

    @Test
    void testItemSales_setters() {
        ItemSales s = new ItemSales();
        s.setItem(Item.QDC_A101);
        s.setPrice(135.50);
        s.setSalesTotal(3L);
        s.setRevenue(406.50);
        assertEquals(Item.QDC_A101, s.item);
        assertEquals(135.50, s.price);
        assertEquals(3L, s.salesTotal);
        assertEquals(406.50, s.revenue);
        assertNull(s.getStoreServerSales());
    }

    // ── LocationOrders ────────────────────────────────────────────────────────

    @Test
    void testLocationOrders() {
        LocationOrders lo = new LocationOrders("TOKYO", Collections.emptyList());
        assertEquals("TOKYO", lo.location);
        assertNotNull(lo.orders);
        assertTrue(lo.orders.isEmpty());
    }
}
