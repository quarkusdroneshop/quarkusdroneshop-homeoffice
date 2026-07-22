package io.quarkusdroneshop.homeoffice.infrastructure;

import io.apicurio.registry.serde.avro.AvroKafkaDeserializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * dataproduct-order-events (order-events Flink ジョブが発行するハブ) から
 * LINE_ITEM_STATUS_CHANGED イベントのみを OrderUpMessage として取り出す。
 * 以前は QDCA10/QDCA10pro が発行する生トピック (shop-bsite.orders-up) を直接購読して
 * いたが、同じ情報は既に dataproduct-order-events として governed な形で公開されて
 * いるため、それに統一する (orders-created の ORDER_PLACED 統一と同じ方針)。
 * それ以外の eventType (ORDER_PLACED, ORDER_CANCELLED) は null を返し破棄する。
 */
public class OrderUpMessageAvroDeserializer implements Deserializer<OrderUpMessage> {

    private static final Logger logger = LoggerFactory.getLogger(OrderUpMessageAvroDeserializer.class);

    private final AvroKafkaDeserializer<GenericRecord> avroDeserializer = new AvroKafkaDeserializer<>();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        avroDeserializer.configure(configs, isKey);
    }

    @Override
    public OrderUpMessage deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            GenericRecord record = avroDeserializer.deserialize(topic, new RecordHeaders(), data);
            if (record == null) {
                return null;
            }

            Object eventTypeObj = record.get("eventType");
            if (eventTypeObj == null || !"LINE_ITEM_STATUS_CHANGED".equals(eventTypeObj.toString())) {
                return null;
            }

            GenericRecord lineItem = (GenericRecord) record.get("lineItem");
            if (lineItem == null) {
                return null;
            }

            OrderUpMessage message = new OrderUpMessage();
            message.orderId = asString(record.get("orderId"));
            message.lineItemId = asString(lineItem.get("itemId"));
            message.item = asString(lineItem.get("item"));
            message.name = asString(lineItem.get("name"));
            message.madeBy = asString(lineItem.get("madeBy"));
            return message;
        } catch (Exception e) {
            logger.warn("Failed to deserialize OrderUpMessage record", e);
            return null;
        }
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    @Override
    public void close() {
        avroDeserializer.close();
    }
}
