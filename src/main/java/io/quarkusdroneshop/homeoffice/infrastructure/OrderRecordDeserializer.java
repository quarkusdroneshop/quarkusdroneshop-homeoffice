package io.quarkusdroneshop.homeoffice.infrastructure;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkusdroneshop.homeoffice.infrastructure.domain.OrderRecord;
import org.jboss.logging.Logger;

public class OrderRecordDeserializer extends ObjectMapperDeserializer<OrderRecord> {

    private static final Logger LOG = Logger.getLogger(OrderRecordDeserializer.class);

    public OrderRecordDeserializer() {
        super(OrderRecord.class);
    }

    // orders-in は earliest-offset から読むため、Item enum のリネーム
    // (QDC_A105_PRO01 -> QDC_A105_Pro01 等) 以前に publish された旧形式の
    // メッセージがトピックに残っている。デシリアライズ失敗のたびに
    // コンシューマが落ちて再起動ループになっていたため、パース失敗レコードは
    // スキップして処理を継続する (Flink 側の json.ignore-parse-errors と同じ方針)。
    @Override
    public OrderRecord deserialize(String topic, byte[] data) {
        try {
            return super.deserialize(topic, data);
        } catch (Exception e) {
            LOG.warnf(e, "Skipping unparseable OrderRecord on topic %s", topic);
            return null;
        }
    }
}