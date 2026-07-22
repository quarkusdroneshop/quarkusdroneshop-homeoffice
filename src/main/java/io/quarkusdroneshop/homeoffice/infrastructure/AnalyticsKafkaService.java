package io.quarkusdroneshop.homeoffice.infrastructure;

import io.quarkusdroneshop.homeoffice.viewmodels.AssemblyLeadTime;
import io.quarkusdroneshop.homeoffice.viewmodels.SalesTrend;
import io.quarkusdroneshop.homeoffice.viewmodels.StockoutRisk;
import org.apache.avro.generic.GenericRecord;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

/**
 * dataproducts の分析系トピック (sales-trends / assembly-lead-time / inventory-analytics)
 * を購読し AnalyticsService のキャッシュへ反映する。全て読み取り専用の分析データであり、
 * ドメイン処理 (KafkaService の Order 更新系) とは無関係のため別クラスに分離している。
 */
@ApplicationScoped
public class AnalyticsKafkaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsKafkaService.class);

    @Inject
    AnalyticsService analyticsService;

    @Incoming("sales-trends-5m")
    public void onSalesTrend5m(final GenericRecord record) {
        if (record == null) return;
        analyticsService.addSalesTrend5m(toSalesTrend(record));
    }

    @Incoming("sales-trends-daily")
    public void onSalesTrendDaily(final GenericRecord record) {
        if (record == null) return;
        analyticsService.addSalesTrendDaily(toSalesTrend(record));
    }

    @Incoming("lead-time-qdca10")
    public void onQdca10LeadTime(final GenericRecord record) {
        if (record == null) return;
        analyticsService.addQdca10LeadTime(toLeadTime(record));
    }

    @Incoming("lead-time-qdca10pro")
    public void onQdca10proLeadTime(final GenericRecord record) {
        if (record == null) return;
        analyticsService.addQdca10proLeadTime(toLeadTime(record));
    }

    @Incoming("inventory-turnover")
    public void onInventoryTurnover(final GenericRecord record) {
        if (record == null) return;
        try {
            String item = record.get("item").toString();
            long windowStart = (Long) record.get("windowStart");
            long windowEnd = (Long) record.get("windowEnd");
            long restockRequestedCount = (Long) record.get("restockRequestedCount");
            long stockoutCancelledCount = (Long) record.get("stockoutCancelledCount");
            analyticsService.applyInventoryTurnover(item, windowStart, windowEnd, restockRequestedCount, stockoutCancelledCount);
        } catch (Exception e) {
            LOGGER.warn("Failed to convert InventoryTurnover record: {}", record, e);
        }
    }

    @Incoming("stockout-risk")
    public void onStockoutRisk(final GenericRecord record) {
        if (record == null) return;
        try {
            String item = record.get("item").toString();
            boolean atRisk = (Boolean) record.get("atRisk");
            long lastStockoutEventAt = (Long) record.get("lastStockoutEventAt");
            analyticsService.applyStockoutRisk(new StockoutRisk(item, atRisk, lastStockoutEventAt));
        } catch (Exception e) {
            LOGGER.warn("Failed to convert StockoutRisk record: {}", record, e);
        }
    }

    private SalesTrend toSalesTrend(GenericRecord record) {
        try {
            String item = record.get("item").toString();
            long windowStart = (Long) record.get("windowStart");
            long windowEnd = (Long) record.get("windowEnd");
            long orderCount = (Long) record.get("orderCount");
            Object revenueRaw = record.get("revenue");
            double revenue = revenueRaw != null ? decodeDecimal((ByteBuffer) revenueRaw, 2).doubleValue() : 0.0;
            Object assemblyLineRaw = record.get("assemblyLine");
            Object locationRaw = record.get("location");
            String assemblyLine = assemblyLineRaw != null ? assemblyLineRaw.toString() : null;
            String location = locationRaw != null ? locationRaw.toString() : null;
            return new SalesTrend(item, windowStart, windowEnd, orderCount, revenue, assemblyLine, location);
        } catch (Exception e) {
            LOGGER.warn("Failed to convert SalesTrend record: {}", record, e);
            return new SalesTrend("UNKNOWN", 0, 0, 0, 0, null, null);
        }
    }

    private AssemblyLeadTime toLeadTime(GenericRecord record) {
        try {
            String itemId = record.get("itemId").toString();
            String orderId = record.get("orderId").toString();
            Object itemRaw = record.get("item");
            String item = itemRaw != null ? itemRaw.toString() : null;
            long placedAt = (Long) record.get("placedAt");
            long fulfilledAt = (Long) record.get("fulfilledAt");
            long leadTimeSeconds = (Long) record.get("leadTimeSeconds");
            String assemblyLine = record.get("assemblyLine").toString();
            return new AssemblyLeadTime(itemId, orderId, item, placedAt, fulfilledAt, leadTimeSeconds, assemblyLine);
        } catch (Exception e) {
            LOGGER.warn("Failed to convert AssemblyLeadTime record: {}", record, e);
            return new AssemblyLeadTime("UNKNOWN", "UNKNOWN", null, 0, 0, 0, "UNKNOWN");
        }
    }

    private static BigDecimal decodeDecimal(ByteBuffer buffer, int scale) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.duplicate().get(bytes);
        return new BigDecimal(new java.math.BigInteger(bytes), scale);
    }
}
