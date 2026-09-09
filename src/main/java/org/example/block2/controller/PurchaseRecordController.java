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
import org.example.block2.dto.*;
import org.example.block2.service.PurchaseRecordService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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
     * Retrieves a list of all purchase records.
     *
     * @return list of purchase records
     */
    @Operation(summary = "Get all purchase records", description = "Returns all purchase records with related material information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Purchase records successfully retrieved",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PurchaseRecordDto.class)))
    })
    @GetMapping
    public List<PurchaseRecordDto> getAllPurchaseRecords() {
        return purchaseRecordService.getAllPurchaseRecords();
    }

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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public PurchaseRecordDto getPurchaseRecord(
            @Parameter(description = "Purchase record ID", required = true) @PathVariable("id") @Positive Long id) {
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Material not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Purchase record already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseRecordDto createPurchaseRecord(
            @Valid @RequestBody PurchaseRecordSaveDto dto) {
        return purchaseRecordService.savePurchaseRecord(dto);
    }

    /**
     * Updates an existing purchase record.
     *
     * @param id  purchase record ID
     * @param dto updated purchase record data
     */
    @Operation(summary = "Update purchase record", description = "Updates an existing purchase record with new data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Purchase record updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid purchase record data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Purchase record or material not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Purchase record already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePurchaseRecord(
            @Parameter(description = "Purchase record ID", required = true) @PathVariable("id") @Positive Long id,
            @Valid @RequestBody PurchaseRecordSaveDto dto) {
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePurchaseRecord(
            @Parameter(description = "Purchase record ID", required = true) @PathVariable("id") @Positive Long id) {
        purchaseRecordService.deletePurchaseRecord(id);
    }

    /**
     * Retrieves purchase records using optional filters and pagination.
     *
     * @param filter filtering and pagination parameters
     * @return paginated list of purchase records
     */
    @Operation(
            summary = "Get purchase records with filtering and pagination",
            description = "Retrieves purchase records using optional order, material and quantity filters"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Purchase records found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PurchaseRecordListResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/_list")
    public PurchaseRecordListResponse getPurchaseRecords(
            @Valid @RequestBody PurchaseRecordListFilterDto filter) {
        return purchaseRecordService.getPurchaseRecords(filter);
    }

    /**
     * Generates CSV report for purchase records using optional filters.
     *
     * @param filter filtering parameters
     * @return CSV file
     */
    @Operation(summary = "Generate purchase records report", description = "Generates a CSV report containing all purchase records matching the specified filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/_report")
    public ResponseEntity<byte[]> generateReport(
            @RequestBody @Valid PurchaseRecordFilterDto filter) {

        byte[] report = purchaseRecordService.generateReport(filter);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"purchase_records.csv\""
                )
                .contentType(MediaType.valueOf("text/csv; charset=UTF-8"))
                .contentLength(report.length)
                .body(report);
    }

    /**
     * Uploads and parses purchase records from JSON file.
     *
     * @param file multipart JSON file
     * @return processing result statistics
     */
    @Operation(summary = "Upload purchase records from file", description = "Parses and imports purchase records from an uploaded JSON file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File processed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PurchaseRecordProcessingResult.class))),
            @ApiResponse(responseCode = "400", description = "Uploaded file is empty or invalid",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    public PurchaseRecordProcessingResult uploadPurchaseRecords(
            @Parameter(description = "JSON file with purchase records", required = true)
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        return purchaseRecordService.importPurchaseRecords(
                file.getInputStream()
        );
    }
}