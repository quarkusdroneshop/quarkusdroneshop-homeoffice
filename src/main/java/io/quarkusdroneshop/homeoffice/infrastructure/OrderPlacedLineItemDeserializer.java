package io.quarkusdroneshop.homeoffice.infrastructure;

import io.apicurio.registry.serde.avro.AvroKafkaDeserializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * dataproduct-order-events (order-events Flink ジョブが発行するハブ) から
 * ORDER_PLACED イベントのみを、注文組み立て用の明細1件 (OrderPlacedLineItem) として取り出す。
 * それ以外の eventType (LINE_ITEM_STATUS_CHANGED, ORDER_CANCELLED) は null を返し破棄する。
 */
public class OrderPlacedLineItemDeserializer implements Deserializer<OrderPlacedLineItem> {

    private static final Logger logger = LoggerFactory.getLogger(OrderPlacedLineItemDeserializer.class);

    private final AvroKafkaDeserializer<GenericRecord> avroDeserializer = new AvroKafkaDeserializer<>();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        avroDeserializer.configure(configs, isKey);
    }

    @Override
    public OrderPlacedLineItem deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            GenericRecord record = avroDeserializer.deserialize(topic, new RecordHeaders(), data);
            if (record == null) {
                return null;
            }

            Object eventTypeObj = record.get("eventType");
            if (eventTypeObj == null || !"ORDER_PLACED".equals(eventTypeObj.toString())) {
                return null;
            }

            GenericRecord lineItem = (GenericRecord) record.get("lineItem");
            if (lineItem == null) {
                return null;
            }

            String orderId = asString(record.get("orderId"));
            String orderSource = asString(record.get("orderSource"));
            String location = asString(record.get("location"));
            String loyaltyMemberId = asString(record.get("loyaltyMemberId"));
            String itemId = asString(lineItem.get("itemId"));
            String item = asString(lineItem.get("item"));
            String name = asString(lineItem.get("name"));
            BigDecimal price = asDecimal(lineItem.get("price"));
            String assemblyLine = asString(lineItem.get("assemblyLine"));

            return new OrderPlacedLineItem(orderId, orderSource, location, loyaltyMemberId,
                    itemId, item, name, price, assemblyLine);
        } catch (Exception e) {
            logger.warn("Failed to deserialize OrderPlacedLineItem record", e);
            return null;
        }
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    // lineItem.price は Avro の bytes decimal(10,2)。AvroKafkaDeserializer は decimal 論理型を
    // 自動変換しないため、ByteBuffer で返ってきても scale=2 の unscaled BigInteger として復元する。
    private static BigDecimal asDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof ByteBuffer) {
            ByteBuffer buf = ((ByteBuffer) value).duplicate();
            byte[] bytes = new byte[buf.remaining()];
            buf.get(bytes);
            return new BigDecimal(new BigInteger(bytes), 2);
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void close() {
        avroDeserializer.close();
    }
}
