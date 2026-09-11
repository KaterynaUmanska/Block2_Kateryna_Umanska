package org.example.block2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.block2.data.MaterialData;
import org.example.block2.data.PurchaseRecordData;
import org.example.block2.dto.*;
import org.example.block2.exception.DuplicateResourceException;
import org.example.block2.exception.ResourceNotFoundException;
import org.example.block2.monitor.Monitored;
import org.example.block2.repository.MaterialRepository;
import org.example.block2.repository.PurchaseRecordRepository;
import org.example.block2.repository.PurchaseRecordSpecifications;
import org.example.block2.utils.JsonStreamParser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Validator;
import org.hibernate.exception.ConstraintViolationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

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
    private final PurchaseRecordImportService purchaseRecordImportService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Creates a new purchase record.
     *
     * @param dto purchase record data
     * @return created purchase record DTO
     * @throws DuplicateResourceException if duplicate or invalid data
     */
    @Monitored
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
            entityManager.flush();

            log.info("Successfully saved purchase record with ID: {}", saved.getId());
            return convertToDto(saved);

        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException(
                    "Purchase record for order ID '%d' and material ID '%d' already exists"
                            .formatted(dto.getOrderId(), dto.getMaterialId())
            );
        }
    }


    /**
     * Returns purchase record DTO by ID.
     *
     * @param id purchase record ID
     * @return purchase record DTO
     */
    @Monitored
    @Transactional(readOnly = true)
    public PurchaseRecordDto getPurchaseRecordById(Long id) {
        log.debug("Fetching purchase record by ID: {}", id);

        PurchaseRecordData data = purchaseRecordRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Purchase record with ID {} not found", id);
                    return new ResourceNotFoundException(
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
    @Monitored
    @Transactional
    public void updatePurchaseRecord(Long id, PurchaseRecordSaveDto dto) {
        log.info("Attempting to update purchase record with ID: {}", id);

        PurchaseRecordData record = purchaseRecordRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Purchase record with ID {} not found", id);
                    return new ResourceNotFoundException(
                            "Purchase record not found with id: " + id
                    );
                });

        MaterialData material = findMaterial(dto.getMaterialId());

        record.setOrderId(dto.getOrderId());
        record.setMaterial(material);
        record.setQuantity(dto.getQuantity());

        try {
            purchaseRecordRepository.save(record);
            entityManager.flush();

            log.info("Successfully updated purchase record with ID: {}", id);
        } catch (DataIntegrityViolationException | ConstraintViolationException ex) {
            throw new DuplicateResourceException(
                    "Purchase record for order ID '%d' and material ID '%d' already exists"
                            .formatted(dto.getOrderId(), dto.getMaterialId())
            );
        }
    }

    /**
     * Deletes purchase record by ID.
     *
     * @param id purchase record ID
     */
    @Monitored
    @Transactional
    public void deletePurchaseRecord(Long id) {
        log.info("Attempting to delete purchase record with ID: {}", id);

        PurchaseRecordData record = purchaseRecordRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Purchase record with ID {} not found", id);
                    return new ResourceNotFoundException(
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
    @Monitored
    public PurchaseRecordListResponse getPurchaseRecords(
            PurchaseRecordListFilterDto filter) {

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

        Specification<PurchaseRecordData> spec = Specification
                .where(PurchaseRecordSpecifications.hasOrderId(filter.getOrderId()))
                .and(PurchaseRecordSpecifications.hasMaterialName(materialName))
                .and(PurchaseRecordSpecifications.quantityGreaterThanOrEqualTo(filter.getQuantityFrom()))
                .and(PurchaseRecordSpecifications.quantityLessThanOrEqualTo(filter.getQuantityTo()));

        Pageable pageable = PageRequest.of(
                filter.getPage() - 1,
                filter.getSize(),
                Sort.by("id").ascending()
        );

        Page<PurchaseRecordData> result =
                purchaseRecordRepository.findAll(spec, pageable);

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

                    return new ResourceNotFoundException(
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
    @Monitored
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

        Specification<PurchaseRecordData> spec = Specification
                .where(PurchaseRecordSpecifications.hasOrderId(filter.getOrderId()))
                .and(PurchaseRecordSpecifications.hasMaterialName(materialName))
                .and(PurchaseRecordSpecifications.quantityGreaterThanOrEqualTo(filter.getQuantityFrom()))
                .and(PurchaseRecordSpecifications.quantityLessThanOrEqualTo(filter.getQuantityTo()));

        List<PurchaseRecordData> records = purchaseRecordRepository.findAll(spec);

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
            purchaseRecordImportService.saveSingleRecordSafely(dto);
            return true;

        } catch (ResourceNotFoundException
                 | DuplicateResourceException
                 | IllegalArgumentException e) {

            log.warn(
                    "Purchase record was not imported: {}",
                    e.getMessage()
            );

            return false;
        }
    }

    @Transactional
    @Monitored
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