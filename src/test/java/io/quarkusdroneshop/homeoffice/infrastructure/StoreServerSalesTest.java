package io.quarkusdroneshop.homeoffice.infrastructure;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkusdroneshop.homeoffice.domain.Item;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.LineItemRecord;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.OrderRecord;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.Qdca10LineItem;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.Qdca10proLineItem;
import io.quarkusdroneshop.homeoffice.viewmodels.ItemSales;
import io.quarkusdroneshop.homeoffice.viewmodels.StoreServerSales;
import org.junit.jupiter.api.Test;

import jakarta.transaction.Transactional;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class StoreServerSalesTest {

    private LineItemRecord buildRecord(Item item, double price) {
        LineItemRecord rec = new LineItemRecord();
        rec.setItem(item);
        rec.setPrice(price);
        rec.setName(item.name());
        return rec;
    }

    @Test
    @Transactional
    void testPersist_withQdca10Items() {
        OrderRecord rec = new OrderRecord();
        rec.orderPlacedTimestamp = java.time.Instant.now();
        // qdca10 ラインアイテムをセットするためリフレクションは不要 — setterなし
        // toQdca10LineItem / toItemSales は static メソッドで直接テスト
        StoreServerSales sss = new StoreServerSales();
        LineItemRecord lineItem = buildRecord(Item.QDC_A101, 135.50);

        Qdca10LineItem qdca10Item = StoreServerSales.toQdca10LineItem(lineItem, sss);
        assertEquals(Item.QDC_A101, qdca10Item.getItem());
        assertEquals("QDC_A101", qdca10Item.getName());
        assertEquals(135.50, qdca10Item.getPrice());
        assertEquals(sss, qdca10Item.getStoreServerSales());

        Qdca10proLineItem qdca10proItem = StoreServerSales.toQdca10proLineItem(lineItem, sss);
        assertEquals(Item.QDC_A101, qdca10proItem.getItem());

        ItemSales itemSales = StoreServerSales.toItemSales(lineItem, sss);
        assertEquals(Item.QDC_A101, itemSales.item);
        assertEquals(135.50, itemSales.price);
        assertEquals(1L, itemSales.salesTotal);
        assertEquals(135.50, itemSales.revenue);
        assertEquals(sss, itemSales.getStoreServerSales());
    }
}
