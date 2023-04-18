package io.quarkuscoffeeshop.homeoffice.infrastructure.domain;

import io.quarkuscoffeeshop.homeoffice.domain.Item;

public record LineItemRecord(Item item, String name) {
}
