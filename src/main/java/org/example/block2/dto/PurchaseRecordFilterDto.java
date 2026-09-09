package org.example.block2.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

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
     * Purchase required material min quantity.
     */
    @PositiveOrZero(message = "Quantity from must be positive or zero")
    private BigDecimal quantityFrom;

    /**
     * Purchase required material max quantity.
     */
    @PositiveOrZero(message = "Quantity to must be positive or zero")
    private BigDecimal quantityTo;


    @AssertTrue(message = "quantityFrom must be less than or equal to quantityTo")
    private boolean isQuantityRangeValid() {
        if (quantityFrom == null || quantityTo == null) {
            return true;
        }
        return quantityFrom.compareTo(quantityTo) <= 0;
    }
}
