package org.example.block2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.block2.data.MaterialData;
import org.example.block2.data.PurchaseRecordData;
import org.example.block2.dto.*;
import org.example.block2.repository.MaterialRepository;
import org.example.block2.repository.PurchaseRecordRepository;
import org.example.block2.utils.JsonStreamParser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Validator;
import org.example.block2.dto.PurchaseRecordProcessingResult;
import org.example.block2.dto.PurchaseRecordSaveDto;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for purchase record processing operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseRecordService {

    private final PurchaseRecordRepository purchaseRecordRepository;
    private final MaterialRepository materialRepository;
    private final CsvReportService csvReportService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Creates a new purchase record.
     *
     * @param dto purchase record data
     * @return created purchase record DTO
     * @throws IllegalArgumentException if duplicate or invalid data
     */
    @Transactional
    public PurchaseRecordDto savePurchaseRecord(PurchaseRecordSaveDto dto) {
        log.info(
                "Attempting to save purchase record for order ID: {} and material ID: {}",
                dto.getOrderId(),
                dto.getMaterialId()
        );

        MaterialData material = findMaterial(dto.getMaterialId());
        PurchaseRecordData data = convertToData(dto, material);

        try {
            PurchaseRecordData saved = purchaseRecordRepository.save(data);
            entityManager.flush(); // Примусово виконуємо запит у БД, щоб зловити унікальні обмеження

            log.info("Successfully saved purchase record with ID: {}", saved.getId());
            return convertToDto(saved);

        } catch (PersistenceException | DataIntegrityViolationException ex) {
            String message = ex.getMessage();
            if (message != null && message.toLowerCase().contains("uk_order_material")) {
                throw new IllegalArgumentException(
                        "Purchase record for order ID '%d' and material ID '%d' already exists"
                                .formatted(dto.getOrderId(), dto.getMaterialId()), ex
                );
            }
            throw ex;
        }
    }

    /**
     * Returns all purchase records as DTOs.
     *
     * @return list of purchase record DTOs
     */
    @Transactional(readOnly = true)
    public List<PurchaseRecordDto> getAllPurchaseRecords() {
        log.debug("Fetching all purchase records from database");

        List<PurchaseRecordDto> records = purchaseRecordRepository.findAll().stream()
                .map(PurchaseRecordService::convertToDto)
                .toList();

        log.info("Found {} purchase records in total", records.size());

        return records;
    }

    /**
     * Returns purchase record DTO by ID.
     *
     * @param id purchase record ID
     * @return purchase record DTO
     */
    @Transactional(readOnly = true)
    public PurchaseRecordDto getPurchaseRecordById(Long id) {
        log.debug("Fetching purchase record by ID: {}", id);

        PurchaseRecordData data = purchaseRecordRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Purchase record with ID {} not found", id);
                    return new IllegalArgumentException(
                            "Purchase record not found with id: " + id
                    );
                });

        return convertToDto(data);
    }

    /**
     * Updates an existing purchase record.
     *
     * @param id  purchase record ID
     * @param dto new purchase record data
     */
    @Transactional
    public void updatePurchaseRecord(Long id, PurchaseRecordSaveDto dto) {
        log.info("Attempting to update purchase record with ID: {}", id);

        PurchaseRecordData record = purchaseRecordRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Purchase record with ID {} not found", id);
                    return new IllegalArgumentException(
                            "Purchase record not found with id: " + id
                    );
                });

        MaterialData material = findMaterial(dto.getMaterialId());

        if (dto.getOrderId() != null) {
            record.setOrderId(dto.getOrderId());
        }
        record.setMaterial(material);
        if (dto.getQuantity() != null) {
            record.setQuantity(dto.getQuantity());
        }

        purchaseRecordRepository.save(record);
        log.info("Successfully updated purchase record with ID: {}", id);
    }

    /**
     * Deletes purchase record by ID.
     *
     * @param id purchase record ID
     */
    @Transactional
    public void deletePurchaseRecord(Long id) {
        log.info("Attempting to delete purchase record with ID: {}", id);

        PurchaseRecordData record = purchaseRecordRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Purchase record with ID {} not found", id);
                    return new IllegalArgumentException(
                            "Purchase record not found with id: " + id
                    );
                });

        purchaseRecordRepository.delete(record);

        log.info("Successfully deleted purchase record with ID: {}", id);
    }
    /**
     * Returns filtered purchase records with pagination.
     *
     * @param filter filtering and pagination parameters
     * @return paginated list of purchase records
     */
    @Transactional(readOnly = true)
    public PurchaseRecordListResponse getPurchaseRecords(
            PurchaseRecordFilterDto filter) {

        log.debug(
                "Fetching purchase records with filters: orderId={}, materialName={}, " +
                        "quantityFrom={}, quantityTo={}, page={}, size={}",
                filter.getOrderId(),
                filter.getMaterialName(),
                filter.getQuantityFrom(),
                filter.getQuantityTo(),
                filter.getPage(),
                filter.getSize()
        );

        String materialName = filter.getMaterialName();

        if (materialName != null && materialName.isBlank()) {
            materialName = null;
        }

        Pageable pageable = PageRequest.of(
                filter.getPage(),
                filter.getSize()
        );

        Page<PurchaseRecordData> result =
                purchaseRecordRepository.findByFilters(
                        filter.getOrderId(),
                        materialName,
                        filter.getQuantityFrom(),
                        filter.getQuantityTo(),
                        pageable
                );

        List<PurchaseRecordListDto> records = result.getContent()
                .stream()
                .map(PurchaseRecordService::convertToListDto)
                .toList();

        log.info(
                "Found {} purchase records on page {} of {}",
                records.size(),
                filter.getPage(),
                result.getTotalPages()
        );

        return new PurchaseRecordListResponse(
                records,
                result.getTotalPages()
        );
    }

    /**
     * Finds material by ID or throws exception.
     *
     * @param materialId material ID
     * @return material data entity
     */
    private MaterialData findMaterial(Long materialId) {
        return materialRepository.findById(materialId)
                .orElseThrow(() -> {
                    log.warn(
                            "Material with ID {} not found while processing purchase record",
                            materialId
                    );

                    return new IllegalArgumentException(
                            "Material not found with id: " + materialId
                    );
                });
    }

    /**
     * Converts entity to DTO.
     *
     * @param data purchase record entity
     * @return purchase record DTO
     */
    private static PurchaseRecordDto convertToDto(PurchaseRecordData data) {
        if (data == null) {
            return null;
        }

        MaterialDto materialDto = null;
        if (data.getMaterial() != null) {
            materialDto = MaterialDto.builder()
                    .id(data.getMaterial().getId())
                    .name(data.getMaterial().getName())
                    .unit(data.getMaterial().getUnit())
                    .description(data.getMaterial().getDescription())
                    .build();
        }

        return PurchaseRecordDto.builder()
                .id(data.getId())
                .orderId(data.getOrderId())
                .quantity(data.getQuantity())
                .material(materialDto)
                .build();
    }

    /**
     * Converts save DTO to entity.
     *
     * @param dto      save DTO
     * @param material material entity
     * @return purchase record entity
     */
    private static PurchaseRecordData convertToData(PurchaseRecordSaveDto dto, MaterialData material) {
        PurchaseRecordData result = new PurchaseRecordData();
        result.setOrderId(dto.getOrderId());
        result.setMaterial(material);
        result.setQuantity(dto.getQuantity());
        return result;
    }
    /**
     * Converts entity to reduced list DTO.
     *
     * @param data purchase record entity
     * @return reduced purchase record DTO
     */
    private static PurchaseRecordListDto convertToListDto(PurchaseRecordData data) {

        return new PurchaseRecordListDto(data.getId(), data.getOrderId(), data.getMaterial().getName(), data.getQuantity());
    }

    @Transactional(readOnly = true)
    public byte[] generateReport(PurchaseRecordFilterDto filter) {

        log.info(
                "Generating purchase records report with filters: orderId={}, materialName={}, quantityFrom={}, quantityTo={}",
                filter.getOrderId(),
                filter.getMaterialName(),
                filter.getQuantityFrom(),
                filter.getQuantityTo()
        );

        String materialName = filter.getMaterialName();

        if (materialName != null && materialName.isBlank()) {
            materialName = null;
        }

        List<PurchaseRecordData> records = purchaseRecordRepository.findAllByFilters(filter.getOrderId(), materialName, filter.getQuantityFrom(), filter.getQuantityTo());

        byte[] report = csvReportService.generatePurchaseRecordsReport(records);

        log.info("Generated purchase records report with {} records", records.size());
        return report;
    }

    private boolean importPurchaseRecord(PurchaseRecordSaveDto dto) {

        if (!validator.validate(dto).isEmpty()) {
            log.warn("Invalid purchase record skipped");
            return false;
        }

        try {
            savePurchaseRecord(dto);
            return true;
        } catch (IllegalArgumentException e) {
            log.warn(
                    "Purchase record was not imported: {}",
                    e.getMessage()
            );
            return false;
        }
    }
    @Transactional
    public PurchaseRecordProcessingResult importPurchaseRecords(
            InputStream inputStream
    ) throws IOException {

        AtomicInteger successful = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();

        JsonStreamParser parser = new JsonStreamParser(objectMapper);

        parser.parse(inputStream, dto -> {
            if (importPurchaseRecord(dto)) {
                successful.incrementAndGet();
            } else {
                failed.incrementAndGet();
            }
        });

        return PurchaseRecordProcessingResult.builder()
                .successful(successful.get())
                .failed(failed.get())
                .build();
    }


}