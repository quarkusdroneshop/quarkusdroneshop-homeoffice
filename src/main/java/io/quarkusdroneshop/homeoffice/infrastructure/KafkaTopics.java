package io.quarkusdroneshop.homeoffice.infrastructure;

public class KafkaTopics {

    public static final String ORDERS_CREATED = "orders-created";

    public static final String ORDERS_UPDATED = "orders-updated";

    public static final String LOYALTY_MEMBER_PURCHASE = "loyalty-member-purchase";

    public static final String ORDER_RETRY_OUT = "order-retry-out";

    public static final String ORDER_RETRY_IN = "order-retry-in";

    public static final String QDCA10_RETRY_OUT = "qdca10-retry-out";

    public static final String QDCA10PRO_RETRY_OUT = "qdca10pro-retry-out";

}
