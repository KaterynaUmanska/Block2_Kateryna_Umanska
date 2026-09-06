package org.example.block2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * DTO for the list response .
 */
@Getter
@AllArgsConstructor
public class PurchaseRecordListDto {

    /**
     * Purchase order ID.
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
    private Double quantity;
}
