package io.quarkuscoffeeshop.homeoffice.infrastructure.domain;

import io.quarkuscoffeeshop.homeoffice.domain.OrderSource;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record OrderRecord(String orderId, OrderSource orderSource, EventType eventType, String loyaltyMemberId,
                          Instant timestamp, List<LineItemRecord> baristaLineItems, List<LineItemRecord> kitchenLineItems) {

}
