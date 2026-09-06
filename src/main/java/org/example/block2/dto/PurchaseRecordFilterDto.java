package org.example.block2.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for filtering purchase records.
 */
@Getter
@Setter
public class PurchaseRecordFilterDto {

    /**
     * ID of the purchase order.
     */
    private Long orderId;

    /**
     * Purchase required material name.
     */
    private String materialName;

    /**
     * Purchase required material  min quantity.
     */
    @PositiveOrZero
    private Double quantityFrom;

    /**
     * Purchase required material max quantity.
     */
    @PositiveOrZero
    private Double quantityTo;

    /**
     * Page number for pagination .
     */
    @Min(0)
    private int page = 0;

    /**
     * Number of items per page.
     */
    @Min(1)
    private int size = 10;
}
