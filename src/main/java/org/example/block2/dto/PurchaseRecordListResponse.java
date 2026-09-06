package org.example.block2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PurchaseRecordListResponse {

    private List<PurchaseRecordListDto> list;
    private int totalPages;
}