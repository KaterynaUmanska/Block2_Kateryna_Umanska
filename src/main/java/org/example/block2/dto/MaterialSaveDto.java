package org.example.block2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import io.swagger.v3.oas.annotations.media.Schema;
import org.example.block2.dict.Unit;

/**
 * DTO for creating a new material.
 */
@Getter
@Builder
@Jacksonized
@Schema(description = "Data for creating or updating a material")
public class MaterialSaveDto {

    /**
     * Material name.
     */
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 255, message = "Name must be up to 255 characters")
    @Schema(description = "Unique material name", example = "Material_1")
    private String name;

    /**
     * Material description.
     */
    @Size(max = 1000)
    @Schema(description = "Material description", example = "High quality structural steel")
    private String description;

    /**
     * Material unit.
     */
    @NotNull(message = "Unit cannot be null")
    @Schema(description = "Material units of measurement", example = "KG")
    private Unit unit;
}
