-- Item enum の実際の定数名は QDC_A105_Pro01〜Pro04 (混在ケース) だが、
-- productsales/productitemsales の CHECK 制約は QDC_A105_PRO01 (全て大文字) を
-- 期待しており、Pro系アイテムの注文実績INSERTが常に失敗していた。
-- 制約側を実際の enum 値に合わせて修正する。

-- 旧全大文字表記 (QDC_A105_PRO01 等) で保存された既存行を現行 enum 表記に正規化
UPDATE droneshop.productsales SET item = 'QDC_A105_Pro01' WHERE item = 'QDC_A105_PRO01';
UPDATE droneshop.productsales SET item = 'QDC_A105_Pro02' WHERE item = 'QDC_A105_PRO02';
UPDATE droneshop.productsales SET item = 'QDC_A105_Pro03' WHERE item = 'QDC_A105_PRO03';
UPDATE droneshop.productsales SET item = 'QDC_A105_Pro04' WHERE item = 'QDC_A105_PRO04';

UPDATE droneshop.productitemsales SET item = 'QDC_A105_Pro01' WHERE item = 'QDC_A105_PRO01';
UPDATE droneshop.productitemsales SET item = 'QDC_A105_Pro02' WHERE item = 'QDC_A105_PRO02';
UPDATE droneshop.productitemsales SET item = 'QDC_A105_Pro03' WHERE item = 'QDC_A105_PRO03';
UPDATE droneshop.productitemsales SET item = 'QDC_A105_Pro04' WHERE item = 'QDC_A105_PRO04';

UPDATE droneshop.itemsales SET item = 'QDC_A105_Pro01' WHERE item = 'QDC_A105_PRO01';
UPDATE droneshop.itemsales SET item = 'QDC_A105_Pro02' WHERE item = 'QDC_A105_PRO02';
UPDATE droneshop.itemsales SET item = 'QDC_A105_Pro03' WHERE item = 'QDC_A105_PRO03';
UPDATE droneshop.itemsales SET item = 'QDC_A105_Pro04' WHERE item = 'QDC_A105_PRO04';

ALTER TABLE droneshop.productsales
    DROP CONSTRAINT IF EXISTS productsales_item_check;

ALTER TABLE droneshop.productsales
    ADD CONSTRAINT productsales_item_check
    CHECK (item::text = ANY (ARRAY[
        'QDC_A101', 'QDC_A102', 'QDC_A103', 'QDC_A104_AC', 'QDC_A104_AT',
        'QDC_A105_Pro01', 'QDC_A105_Pro02', 'QDC_A105_Pro03', 'QDC_A105_Pro04'
    ]::text[]));

ALTER TABLE droneshop.productitemsales
    DROP CONSTRAINT IF EXISTS productitemsales_item_check;

ALTER TABLE droneshop.productitemsales
    ADD CONSTRAINT productitemsales_item_check
    CHECK (item::text = ANY (ARRAY[
        'QDC_A101', 'QDC_A102', 'QDC_A103', 'QDC_A104_AC', 'QDC_A104_AT',
        'QDC_A105_Pro01', 'QDC_A105_Pro02', 'QDC_A105_Pro03', 'QDC_A105_Pro04'
    ]::text[]));

ALTER TABLE droneshop.itemsales
    DROP CONSTRAINT IF EXISTS itemsales_item_check;

ALTER TABLE droneshop.itemsales
    ADD CONSTRAINT itemsales_item_check
    CHECK (item::text = ANY (ARRAY[
        'QDC_A101', 'QDC_A102', 'QDC_A103', 'QDC_A104_AC', 'QDC_A104_AT',
        'QDC_A105_Pro01', 'QDC_A105_Pro02', 'QDC_A105_Pro03', 'QDC_A105_Pro04'
    ]::text[]));
