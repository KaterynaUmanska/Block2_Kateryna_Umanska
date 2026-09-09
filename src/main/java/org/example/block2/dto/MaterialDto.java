package org.example.block2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.example.block2.dict.Unit;

/**
 * DTO for material details with associated purchase records.
 */
@Getter
@Builder
@Jacksonized
@Schema(description = "Material details")
public class MaterialDto {

    /**
     * Material ID.
     */
    @Schema(description = "Unique material identifier", example = "10")
    private Long id;

    /**
     * Material name.
     */
    @Schema(description = "Material name", example = "Steel")
    private String name;

    /**
     * Unit of measurement.
     */
    @Schema(description = "Unit of measurement", example = "KG")
    private Unit unit;

    /**
     * Material description.
     */
    @Schema(description = "Material description", example = "High-strength structural steel")
    private String description;

}
