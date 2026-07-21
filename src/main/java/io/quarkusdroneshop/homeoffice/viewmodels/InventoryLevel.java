package io.quarkusdroneshop.homeoffice.viewmodels;

/**
 * inventory サービス (bsite) の /inventory REST API から取得した在庫状況。
 * 各サービスの Item enum 名は QDC_A105_Pro01 のように統一されているが、
 * 品目カタログをサービスごとに独自の enum として重複定義しているため、
 * 依存を増やさずに済むよう enum ではなく文字列としてそのまま受け渡す。
 */
public class InventoryLevel {

    public String item;

    public int inStockQuantity;

    public InventoryLevel() {
    }

    public InventoryLevel(String item, int inStockQuantity) {
        this.item = item;
        this.inStockQuantity = inStockQuantity;
    }
}
