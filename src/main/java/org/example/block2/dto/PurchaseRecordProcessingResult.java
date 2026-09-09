package org.example.block2.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * Result of purchase record import operation.
 */
@Getter
@Builder
@Jacksonized
@Schema(description = "Result of purchase record import operation")
public class PurchaseRecordProcessingResult {

    @Schema(description = "Number of successfully imported records", example = "8")
    private int successful;

    @Schema(description = "Number of records that failed to import", example = "2")
    private int failed;
}
