package org.example.block2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.example.block2.dto.MaterialDto;
import org.example.block2.dto.MaterialSaveDto;
import org.example.block2.service.MaterialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for material operations.
 */
@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
@Tag(name = "Materials", description = "API for managing materials")
public class MaterialController {

    private final MaterialService materialService;

    /**
     * Retrieves a list of all materials.
     *
     * @return list of material details
     */
    @Operation(summary = "Get all materials", description = "Retrieves a list of all available materials in the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Materials found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MaterialDto.class)))
    })
    @GetMapping
    public List<MaterialDto> getAllMaterials() {
        return materialService.getAllMaterials();
    }

    /**
     * Retrieves material details by ID.
     *
     * @param id material ID
     * @return material details
     */
    @Operation(summary = "Get material details", description = "Retrieves material details by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Material found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MaterialDto.class))),
            @ApiResponse(responseCode = "404", description = "Material not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public MaterialDto getMaterial(
            @Parameter(description = "Material ID", required = true) @PathVariable("id") @Positive Long id) {
        return materialService.getMaterialById(id);
    }

    /**
     * Creates a new material.
     *
     * @param dto material data
     * @return created material details
     */
    @Operation(summary = "Create material", description = "Creates a new material with the provided data and checks for name uniqueness")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Material created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MaterialDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid material data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Material name already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaterialDto createMaterial(
            @Valid @RequestBody MaterialSaveDto dto) {
        return materialService.saveMaterial(dto);
    }

    /**
     * Updates an existing material.
     *
     * @param id  material ID
     * @param dto updated material data
     */
    @Operation(summary = "Update material", description = "Updates an existing material with new data considering name uniqueness")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Material updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid material data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Material not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Material name already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMaterial(
            @Parameter(description = "Material ID", required = true) @PathVariable("id") @Positive Long id,
            @Valid @RequestBody MaterialSaveDto dto) {
        materialService.updateMaterial(id, dto);
    }

    /**
     * Deletes a material.
     *
     * @param id material ID
     */
    @Operation(summary = "Delete material", description = "Deletes a material by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Material deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Material not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Material cannot be deleted because it is referenced by purchase records",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMaterial(
            @Parameter(description = "Material ID", required = true) @PathVariable("id") @Positive Long id) {
        materialService.deleteMaterial(id);
    }
}