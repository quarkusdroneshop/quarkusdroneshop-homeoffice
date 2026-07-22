package io.quarkusdroneshop.homeoffice.infrastructure;

import io.quarkusdroneshop.homeoffice.viewmodels.AssemblyLeadTime;
import io.quarkusdroneshop.homeoffice.viewmodels.InventoryTurnover;
import io.quarkusdroneshop.homeoffice.viewmodels.SalesTrend;
import io.quarkusdroneshop.homeoffice.viewmodels.StockoutRisk;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * dataproducts の各種分析トピック (Kafka, Avro) を購読してローカルにキャッシュする。
 * Trino OIDC 経由でのクエリはインフラ整備コスト (asite への外部 Route 新設 +
 * Keycloak クライアント発行 + JDBC からの OAuth2 トークン取得) が大きいため見送り、
 * 既存の Inventory 連携と同じ方針 (dataproduct.* トピックを直接購読) を採用する。
 * 追記専用 (sales-trends / lead-time / inventory-turnover) は直近 N 件のみ保持する
 * 簡易な bounded cache とし、恒久的な集計・保存は Trino/Iceberg 側の役割のままとする。
 */
@ApplicationScoped
public class AnalyticsService {

    private static final int MAX_HISTORY = 500;

    private final Deque<SalesTrend> salesTrends5m = new ArrayDeque<>();
    private final Deque<SalesTrend> salesTrendsDaily = new ArrayDeque<>();
    private final Deque<AssemblyLeadTime> qdca10LeadTimes = new ArrayDeque<>();
    private final Deque<AssemblyLeadTime> qdca10proLeadTimes = new ArrayDeque<>();

    // inventory_turnover は item・window ごとに「補充要求数」「欠品キャンセル数」の
    // 部分行が別々に届く (Flink 側が append-only 制約を回避するため)。Trino ビューと
    // 同様に item+window 単位で合算してから保持する。
    private final Map<String, InventoryTurnover> inventoryTurnover = new ConcurrentHashMap<>();

    private final Map<String, StockoutRisk> stockoutRisk = new ConcurrentHashMap<>();

    private static void addBounded(Deque<?> deque, Object item) {
        synchronized (deque) {
            @SuppressWarnings("unchecked")
            Deque<Object> d = (Deque<Object>) deque;
            d.addLast(item);
            while (d.size() > MAX_HISTORY) {
                d.removeFirst();
            }
        }
    }

    static BigDecimal decodeDecimal(ByteBuffer buffer, int scale) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.duplicate().get(bytes);
        return new BigDecimal(new java.math.BigInteger(bytes), scale);
    }

    public void addSalesTrend5m(SalesTrend trend) {
        addBounded(salesTrends5m, trend);
    }

    public void addSalesTrendDaily(SalesTrend trend) {
        addBounded(salesTrendsDaily, trend);
    }

    public void addQdca10LeadTime(AssemblyLeadTime leadTime) {
        addBounded(qdca10LeadTimes, leadTime);
    }

    public void addQdca10proLeadTime(AssemblyLeadTime leadTime) {
        addBounded(qdca10proLeadTimes, leadTime);
    }

    public void applyInventoryTurnover(String item, long windowStart, long windowEnd,
            long restockRequestedDelta, long stockoutCancelledDelta) {
        String key = item + "|" + windowStart;
        inventoryTurnover.compute(key, (k, existing) -> {
            if (existing == null) {
                return new InventoryTurnover(item, windowStart, windowEnd, restockRequestedDelta, stockoutCancelledDelta);
            }
            existing.restockRequestedCount += restockRequestedDelta;
            existing.stockoutCancelledCount += stockoutCancelledDelta;
            return existing;
        });
    }

    public void applyStockoutRisk(StockoutRisk risk) {
        stockoutRisk.put(risk.item, risk);
    }

    public List<SalesTrend> getSalesTrends5m() {
        synchronized (salesTrends5m) {
            return List.copyOf(salesTrends5m);
        }
    }

    public List<SalesTrend> getSalesTrendsDaily() {
        synchronized (salesTrendsDaily) {
            return List.copyOf(salesTrendsDaily);
        }
    }

    public List<AssemblyLeadTime> getQdca10LeadTimes() {
        synchronized (qdca10LeadTimes) {
            return List.copyOf(qdca10LeadTimes);
        }
    }

    public List<AssemblyLeadTime> getQdca10proLeadTimes() {
        synchronized (qdca10proLeadTimes) {
            return List.copyOf(qdca10proLeadTimes);
        }
    }

    public List<InventoryTurnover> getInventoryTurnover() {
        return inventoryTurnover.values().stream()
            .sorted((a, b) -> Long.compare(b.windowStart, a.windowStart))
            .collect(Collectors.toList());
    }

    public List<StockoutRisk> getStockoutRisk() {
        return List.copyOf(stockoutRisk.values());
    }
}
