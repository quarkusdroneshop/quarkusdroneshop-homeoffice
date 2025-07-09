package io.quarkusdroneshop.homeoffice.domain;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Item {
    // Beverages
    QDC_A101(135.50), QDC_A102(155.50), QDC_A103(144.00),QDC_A104_AC(256.25), QDC_A104_AT(4.75),

    // Food
    QDC_A105_PRO01(553.00), QDC_A105_PRO02(633.25), QDC_A105_PRO03(735.50), QDC_A105_PRO04(955.50);

    private final BigDecimal price;

    Item(double price) {
        this.price = BigDecimal.valueOf(price);
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    @JsonCreator
    public static Item fromString(String key) {
        return key == null ? null : Item.valueOf(key.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}