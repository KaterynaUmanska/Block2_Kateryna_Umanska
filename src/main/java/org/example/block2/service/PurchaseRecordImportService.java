package org.example.block2.service;

import lombok.RequiredArgsConstructor;
import org.example.block2.data.MaterialData;
import org.example.block2.data.PurchaseRecordData;
import org.example.block2.dto.PurchaseRecordSaveDto;
import org.example.block2.exception.DuplicateResourceException;
import org.example.block2.exception.ResourceNotFoundException;
import org.example.block2.repository.MaterialRepository;
import org.example.block2.repository.PurchaseRecordRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for importing individual purchase records.
 */
@Service
@RequiredArgsConstructor
public class PurchaseRecordImportService {

    private final PurchaseRecordRepository purchaseRecordRepository;
    private final MaterialRepository materialRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Saves one purchase record in an independent transaction.
     *
     * @param dto purchase record data
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSingleRecordSafely(PurchaseRecordSaveDto dto) {

        MaterialData material = materialRepository.findById(dto.getMaterialId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Material not found with id: " + dto.getMaterialId()
                        )
                );

        PurchaseRecordData data = new PurchaseRecordData();
        data.setOrderId(dto.getOrderId());
        data.setMaterial(material);
        data.setQuantity(dto.getQuantity());

        try {
            purchaseRecordRepository.save(data);

            // Force INSERT so the unique constraint is checked
            // inside this independent transaction.
            entityManager.flush();

        } catch (DataIntegrityViolationException ex) {
            if (ex.getMessage() != null
                    && ex.getMessage().toLowerCase().contains("uk_order_material")) {

                throw new DuplicateResourceException(
                        "Purchase record for order ID '%d' and material ID '%d' already exists"
                                .formatted(
                                        dto.getOrderId(),
                                        dto.getMaterialId()
                                )
                );
            }

            throw ex;
        }
    }
}
