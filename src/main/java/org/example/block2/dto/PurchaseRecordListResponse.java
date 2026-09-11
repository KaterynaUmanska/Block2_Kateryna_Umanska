package org.example.block2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Response DTO for a paginated list of purchase records.
 */
@Getter
@AllArgsConstructor
public class PurchaseRecordListResponse {

    private List<PurchaseRecordListDto> list;
    private int totalPages;
}