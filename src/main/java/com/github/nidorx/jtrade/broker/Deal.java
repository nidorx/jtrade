package com.github.nidorx.jtrade.broker;

import com.github.nidorx.jtrade.broker.enums.DealType;
import com.github.nidorx.jtrade.broker.enums.DealEntry;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 *
 * @author Alex Rodin <contato@alexrodin.info>
 */
@Data
@Builder
@AllArgsConstructor
public class Deal {

    private final Long id;

    private final Long order;

    private final Long position;

    private final Instant time;

    private final DealType type;

    private final DealEntry entry;

    private final double price;

    private final double volume;

    // private final double commission;
    // private final double swap;
    // private final double profit;
}
