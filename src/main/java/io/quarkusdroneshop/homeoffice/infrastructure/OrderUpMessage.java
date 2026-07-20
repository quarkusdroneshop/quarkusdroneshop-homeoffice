package io.quarkusdroneshop.homeoffice.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;

/**
 * qdca10 / qdca10pro が publish する OrderUp (shop-bsite.orders-up にミラーされる)
 * の実際の JSON 形状 {orderId, lineItemId, item, name, timestamp, madeBy}。
 * counter から届く HomeofficeOrderMessage（新規注文用）とは全く異なる形状のため、
 * 従来のように OrderRecord で受けるとフィールドが全て null になってしまっていた。
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderUpMessage {

    public String orderId;
    public String lineItemId;
    public String item;
    public String name;
    public Instant timestamp;
    public String madeBy;

    public OrderUpMessage() {
    }
}
