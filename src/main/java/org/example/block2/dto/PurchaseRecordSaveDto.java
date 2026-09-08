package org.example.block2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

/**
 * DTO for creating a new purchase record.
 */
@Getter
@Builder
@Jacksonized
@Schema(description = "Data for creating or updating a purchase record")
public class PurchaseRecordSaveDto {

    /**
     * Purchase order ID.
     */
    @NotNull(message = "Order ID cannot be null")
    @Positive(message = "ID must be greater than 0")
    @Schema(description = "ID of the purchase order", example = "1001", required = true)
    private Long orderId;

    /**
     * Material ID.
     */
    @NotNull(message = "Material ID cannot be null")
    @Positive(message = "ID must be greater than 0")
    @Schema(description = "Material ID associated with this record", example = "1")
    private Long materialId;

    /**
     * Required quantity.
     */
    @NotNull(message = "Quantity cannot be null")
    @DecimalMin(message = "Quantity must be greater than zero", value = "0.01")
    @Schema(description = "Required material quantity", example = "15.5")
    private BigDecimal quantity;
}
