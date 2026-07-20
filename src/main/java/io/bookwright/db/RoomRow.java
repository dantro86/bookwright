package io.bookwright.db;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RoomRow {
    Integer id;
    String name;
    String type;
    BigDecimal price;
}
