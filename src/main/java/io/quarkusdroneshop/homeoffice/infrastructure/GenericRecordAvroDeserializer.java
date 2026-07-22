package io.quarkusdroneshop.homeoffice.infrastructure;

import io.apicurio.registry.serde.avro.AvroKafkaDeserializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * dataproducts の各種分析用トピック (sales-trends / lead-time / inventory-analytics) は
 * いずれも Flink 側で生成した単純な Avro レコードであり、homeoffice はフィールド値を
 * そのまま読み取って表示するだけなので、型ごとの専用クラスを作らず GenericRecord を
 * 直接 @Incoming ハンドラへ渡す共通デシリアライザとして扱う。
 */
public class GenericRecordAvroDeserializer implements Deserializer<GenericRecord> {

    private final AvroKafkaDeserializer<GenericRecord> avroDeserializer = new AvroKafkaDeserializer<>();

    @Override
    public void configure(java.util.Map<String, ?> configs, boolean isKey) {
        avroDeserializer.configure(configs, isKey);
    }

    @Override
    public GenericRecord deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        return avroDeserializer.deserialize(topic, new RecordHeaders(), data);
    }

    @Override
    public void close() {
        avroDeserializer.close();
    }
}
