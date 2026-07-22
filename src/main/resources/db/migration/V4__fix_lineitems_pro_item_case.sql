-- lineitems には CHECK 制約は無いが、Item enum リネーム前 (QDC_A105_PRO01 等、全て大文字)
-- の行が残っており、Order.getLineItems() のコレクション読み込み時に
-- IllegalArgumentException: No enum constant ...Item.QDC_A105_PRO03 で失敗し、
-- ホームオフィスUIの Store Sales / Item Sales Totals / Item Sales Trends
-- グラフが GraphQL の System error で描画できなくなっていた。
UPDATE droneshop.lineitems SET item = 'QDC_A105_Pro01' WHERE item = 'QDC_A105_PRO01';
UPDATE droneshop.lineitems SET item = 'QDC_A105_Pro02' WHERE item = 'QDC_A105_PRO02';
UPDATE droneshop.lineitems SET item = 'QDC_A105_Pro03' WHERE item = 'QDC_A105_PRO03';
UPDATE droneshop.lineitems SET item = 'QDC_A105_Pro04' WHERE item = 'QDC_A105_PRO04';
