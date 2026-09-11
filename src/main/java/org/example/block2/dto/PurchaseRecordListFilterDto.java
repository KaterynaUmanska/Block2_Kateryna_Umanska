package org.example.block2.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO for filtering and paginating purchase records.
 */
@Getter
@Setter
public class PurchaseRecordListFilterDto extends PurchaseRecordFilterDto{

    /**
     * Page number for pagination.
     */
    @Min(1)
    private int page = 1;

    /**
     * Number of items per page.
     */
    @Min(1)
    @Max(100)
    private int size = 10;
}
