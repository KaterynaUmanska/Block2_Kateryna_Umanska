package org.example.block2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.block2.dto.PurchaseRecordDto;
import org.example.block2.dto.PurchaseRecordSaveDto;
import org.example.block2.service.PurchaseRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for purchase record operations.
 */
@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
@Tag(name = "Purchase records", description = "API for managing purchase records")
public class PurchaseRecordController {

    private final PurchaseRecordService purchaseRecordService;

    /**
     * Retrieves purchase record details by ID.
     *
     * @param id purchase record ID
     * @return purchase record details with material
     */
    @Operation(summary = "Get purchase record details", description = "Retrieves purchase record details including associated material")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Purchase record found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PurchaseRecordDto.class))),
            @ApiResponse(responseCode = "404", description = "Purchase record not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public PurchaseRecordDto getPurchaseRecord(
            @Parameter(description = "Purchase record ID", required = true) @PathVariable("id") Long id) {
        return purchaseRecordService.getPurchaseRecordById(id);
    }

    /**
     * Creates a new purchase record.
     *
     * @param dto purchase record data
     * @return created purchase record
     */
    @Operation(summary = "Create purchase record", description = "Creates a new purchase record with the provided data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Purchase record created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PurchaseRecordDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid purchase record data",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Material not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseRecordDto createPurchaseRecord(
            @Parameter(description = "Purchase record data", required = true) @Valid @RequestBody PurchaseRecordSaveDto dto) {
        return purchaseRecordService.savePurchaseRecord(dto);
    }

    /**
     * Updates an existing purchase record.
     *
     * @param id purchase record ID
     * @param dto updated purchase record data
     */
    @Operation(summary = "Update purchase record", description = "Updates an existing purchase record with new data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Purchase record updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid purchase record data",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Purchase record or material not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePurchaseRecord(
            @Parameter(description = "Purchase record ID", required = true) @PathVariable("id") Long id,
            @Parameter(description = "Updated purchase record data", required = true) @Valid @RequestBody PurchaseRecordSaveDto dto) {
        purchaseRecordService.updatePurchaseRecord(id, dto);
    }

    /**
     * Deletes a purchase record.
     *
     * @param id purchase record ID
     */
    @Operation(summary = "Delete purchase record", description = "Deletes a purchase record by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Purchase record deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Purchase record not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePurchaseRecord(
            @Parameter(description = "Purchase record ID", required = true) @PathVariable("id") Long id) {
        purchaseRecordService.deletePurchaseRecord(id);
    }
}