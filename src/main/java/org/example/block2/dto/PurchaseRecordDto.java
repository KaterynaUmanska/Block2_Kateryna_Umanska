package org.example.block2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

/**
 * Purchase record DTO.
 */
@Getter
@Builder
@Jacksonized
@Schema(description = "Purchase record information")
public class PurchaseRecordDto {

    /**
     * Purchase record ID.
     */
    @Schema(description = "Purchase record ID", example = "1")
    private Long id;

    /**
     * ID of the purchase order.
     */
    @Schema(description = "ID of the purchase order", example = "1001")
    private Long orderId;

    /**
     * Purchase record material details.
     */
    @Schema(description = "Material details")
    private MaterialDto material;

    /**
     * Purchase required material quantity.
     */
    @Schema(description = "Required material quantity", example = "15.5")
    private BigDecimal quantity;
}
