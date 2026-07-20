package io.quarkusdroneshop.homeoffice.infrastructure;

import java.time.Instant;

/**
 * counter サービスの OrderTicket / qdca10・qdca10pro の OrderIn と同じ JSON 形状
 * ({orderId, lineItemId, item, name, timestamp}) を持つ再送用チケット。
 * Retry 時にこれを qdca10-in / qdca10pro-in トピックへ直接 publish することで、
 * 物理端末シミュレータに本当のチケットとして再処理させる。
 */
public class RetryOrderTicket {

    public String orderId;
    public String lineItemId;
    public String item;
    public String name;
    public Instant timestamp;

    public RetryOrderTicket() {
    }

    public RetryOrderTicket(String orderId, String lineItemId, String item, String name) {
        this.orderId = orderId;
        this.lineItemId = lineItemId;
        this.item = item;
        this.name = name;
        this.timestamp = Instant.now();
    }
}
