package org.example.block2.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.block2.data.MaterialData;
import org.example.block2.data.PurchaseRecordData;
import org.example.block2.dto.MaterialDto;
import org.example.block2.dto.PurchaseRecordDto;
import org.example.block2.dto.PurchaseRecordSaveDto;
import org.example.block2.repository.MaterialRepository;
import org.example.block2.repository.PurchaseRecordRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}