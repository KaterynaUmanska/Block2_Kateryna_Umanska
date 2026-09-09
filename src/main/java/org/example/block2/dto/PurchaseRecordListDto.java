package org.example.block2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * DTO for the list response .
 */
@Getter
@AllArgsConstructor
public class PurchaseRecordListDto {

    /**
     * Purchase record ID.
     */
    private Long id;

    /**
     * Order ID.
     */
    private Long orderId;

    /**
     * Material name.
     */
    private String materialName;

    /**
     * Required quantity.
     */
    private BigDecimal quantity;
}
