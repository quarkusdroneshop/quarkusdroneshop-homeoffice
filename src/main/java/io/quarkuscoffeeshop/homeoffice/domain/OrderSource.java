package io.quarkuscoffeeshop.homeoffice.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Represents the origin source for the order
 */
@RegisterForReflection
public enum OrderSource {

    COUNTER, WEB, PARTNER;
}
