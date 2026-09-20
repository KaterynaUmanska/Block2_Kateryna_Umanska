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
import org.example.block2.mapper.PurchaseRecordMapper;
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

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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

    private final PurchaseRecordMapper purchaseRecordMapper;

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
        PurchaseRecordData data = purchaseRecordMapper.toEntity(dto, material);

        try {
            PurchaseRecordData saved = purchaseRecordRepository.save(data);
            entityManager.flush();

            log.info("Successfully saved purchase record with ID: {}", saved.getId());
            return purchaseRecordMapper.toDto(saved);

        } catch (DataIntegrityViolationException e) {
            if (isOrderMaterialDuplicate(e)) {
                throw new DuplicateResourceException(
                        "Purchase record for order ID '%d' and material ID '%d' already exists"
                                .formatted(dto.getOrderId(), dto.getMaterialId())
                );
            }

            throw e;
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
                    return new ResourceNotFoundException("Purchase record not found with id: " + id);
                });

        return purchaseRecordMapper.toDto(data);
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
                    log.warn("Cannot update purchase record, purchase record with ID {} not found", id);
                    return new ResourceNotFoundException("Purchase record not found with id: " + id);
                });

        MaterialData material = findMaterial(dto.getMaterialId());

        record.setOrderId(dto.getOrderId());
        record.setMaterial(material);
        record.setQuantity(dto.getQuantity());

        try {
            purchaseRecordRepository.save(record);
            entityManager.flush();

            log.info("Successfully updated purchase record with ID: {}", id);
        } catch (DataIntegrityViolationException e) {
            if (isOrderMaterialDuplicate(e)) {
                throw new DuplicateResourceException(
                        "Purchase record for order ID '%d' and material ID '%d' already exists"
                                .formatted(dto.getOrderId(), dto.getMaterialId())
                );
            }

            throw e;
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
                    log.warn("Cannot delete purchase record, record with ID {} not found", id);
                    return new ResourceNotFoundException(
                            "Purchase record not found with id: " + id);
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

        Specification<PurchaseRecordData> spec = createSpecification(
                filter.getOrderId(),
                filter.getMaterialName(),
                filter.getQuantityFrom(),
                filter.getQuantityTo()
        );

        Pageable pageable = PageRequest.of(
                filter.getPage() - 1,
                filter.getSize(),
                Sort.by("id").ascending()
        );

        Page<PurchaseRecordData> result =
                purchaseRecordRepository.findAll(spec, pageable);

        List<PurchaseRecordListDto> records = result.getContent()
                .stream()
                .map(purchaseRecordMapper::toListDto)
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

    private Specification<PurchaseRecordData> createSpecification(
            Long orderId, String materialName, BigDecimal quantityFrom, BigDecimal quantityTo) {

        if (materialName != null && materialName.isBlank()) {
            materialName = null;
        }

        return Specification
                .where(PurchaseRecordSpecifications.hasOrderId(orderId))
                .and(PurchaseRecordSpecifications.hasMaterialName(materialName))
                .and(PurchaseRecordSpecifications.quantityGreaterThanOrEqualTo(quantityFrom))
                .and(PurchaseRecordSpecifications.quantityLessThanOrEqualTo(quantityTo));
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
     * Generates CSV report for purchase records matching filters.
     *
     * @param filter filters for purchase records
     * @return CSV report as byte array
     */
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

        Specification<PurchaseRecordData> spec = createSpecification(
                filter.getOrderId(),
                filter.getMaterialName(),
                filter.getQuantityFrom(),
                filter.getQuantityTo()
        );

        int pageSize = 1000;
        int pageNumber = 0;
        Page<PurchaseRecordData> page;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

            writer.write("ID,Order ID,Material,Quantity\n");

            long totalRecords = 0;

            do {
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                page = purchaseRecordRepository.findAll(spec, pageable);

                for (PurchaseRecordData record : page.getContent()) {
                    writer.write(String.format("%d,%s,%s,%.2f\n",
                            record.getId(),
                            record.getOrderId(),
                            record.getMaterial() != null ? record.getMaterial().getName() : "",
                            record.getQuantity()
                    ));
                }

                totalRecords += page.getNumberOfElements();
                pageNumber++;

            } while (page.hasNext());

            writer.flush();

            log.info("Successfully generated purchase records report with {} records (paginated)", totalRecords);
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Failed to generate CSV report due to I/O error", e);
            throw new RuntimeException("Error generating CSV report", e);
        }
    }

    /**
     * Validates and safely imports a single purchase record.
     *
     * @param dto purchase record data to import
     * @return true if the record was imported successfully, false otherwise
     */

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

    /**
     * Imports purchase records from JSON input stream.
     *
     * @param inputStream JSON input stream containing purchase records
     * @return processing result with successful and failed record counts
     * @throws IOException if an I/O error occurs while reading the input stream
     */

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

    private boolean isOrderMaterialDuplicate(DataIntegrityViolationException e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof ConstraintViolationException) {
                ConstraintViolationException cve = (ConstraintViolationException) cause;

                if ("uk_order_material".equalsIgnoreCase(cve.getConstraintName())) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }
}