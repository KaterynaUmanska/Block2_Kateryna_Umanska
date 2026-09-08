package org.example.block2.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import org.example.block2.dto.MaterialDto;
import org.example.block2.exeption.DuplicateResourceException;
import org.example.block2.exeption.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.block2.data.MaterialData;
import org.example.block2.dto.MaterialSaveDto;
import org.example.block2.repository.MaterialRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for material management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Creates a new material.
     *
     * @param dto material save DTO
     * @return created material DTO
     * @throws IllegalArgumentException if duplicate name or invalid data
     */
    @Transactional
    public MaterialDto saveMaterial(MaterialSaveDto dto) {
        log.info("Attempting to save new material with name: '{}'", dto.getName());

        MaterialData material = convertToData(dto);

        try {
            MaterialData saved = materialRepository.save(material);
            entityManager.flush(); // Примусово виконуємо запит у БД, щоб зловити унікальне обмеження на ім'я

            log.info("Successfully saved material with ID: {}", saved.getId());
            return convertToDto(saved);

        } catch (PersistenceException | DataIntegrityViolationException ex) {
            String message = ex.getMessage();
            if (message != null && (message.toLowerCase().contains("material") || message.toLowerCase().contains("name"))) {
                throw new DuplicateResourceException(
                        "Material with name '%s' already exists".formatted(dto.getName())
                );
            }
            throw ex;
        }
    }

    /**
     * Returns all materials as DTOs.
     *
     * @return list of material DTOs
     */
    @Transactional(readOnly = true)
    public List<MaterialDto> getAllMaterials() {
        log.debug("Fetching all materials from database");

        List<MaterialDto> materials = materialRepository.findAll().stream()
                .map(MaterialService::convertToDto)
                .toList();

        log.info("Found {} materials in total", materials.size());

        return materials;
    }

    /**
     * Returns material DTO by ID.
     *
     * @param id material ID
     * @return material DTO
     */
    @Transactional(readOnly = true)
    public MaterialDto getMaterialById(Long id) {
        log.debug("Fetching material by ID: {}", id);

        MaterialData data = materialRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Material with ID {} not found", id);
                    return new ResourceNotFoundException(
                            "Material not found with id: " + id
                    );
                });

        return convertToDto(data);
    }

    /**
     * Updates an existing material.
     *
     * @param id  material ID
     * @param dto new material data
     */
    @Transactional
    public void updateMaterial(Long id, MaterialSaveDto dto) {
        log.info("Attempting to update material with ID: {}", id);

        MaterialData material = materialRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Material with ID {} not found", id);
                    return new ResourceNotFoundException(
                            "Material not found with id: " + id
                    );
                });

        if (dto.getName() != null) {
            material.setName(dto.getName());
        }
        if (dto.getUnit() != null) {
            material.setUnit(dto.getUnit());
        }
        material.setDescription(dto.getDescription());

        try {
            materialRepository.save(material);
            entityManager.flush();

            log.info("Successfully updated material with ID: {}", id);
        } catch (PersistenceException | DataIntegrityViolationException ex) {
            String message = ex.getMessage();
            if (message != null && (message.toLowerCase().contains("material") || message.toLowerCase().contains("name"))) {
                throw new DuplicateResourceException(
                        "Material with name '%s' already exists".formatted(dto.getName())
                );
            }
            throw ex;
        }
    }

    /**
     * Deletes material by ID.
     *
     * @param id material ID
     */
    @Transactional
    public void deleteMaterial(Long id) {
        log.info("Attempting to delete material with ID: {}", id);

        MaterialData material = materialRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Material with ID {} not found", id);
                    return new ResourceNotFoundException(
                            "Material not found with id: " + id
                    );
                });

        materialRepository.delete(material);

        log.info("Successfully deleted material with ID: {}", id);
    }

    /**
     * Converts entity to DTO.
     *
     * @param data material entity
     * @return material DTO
     */
    private static MaterialDto convertToDto(MaterialData data) {
        if (data == null) {
            return null;
        }

        return MaterialDto.builder()
                .id(data.getId())
                .name(data.getName())
                .unit(data.getUnit())
                .description(data.getDescription())
                .build();
    }

    /**
     * Converts save DTO to entity.
     *
     * @param dto save DTO
     * @return material entity
     */
    private static MaterialData convertToData(MaterialSaveDto dto) {
        MaterialData result = new MaterialData();
        result.setName(dto.getName());
        result.setUnit(dto.getUnit());
        result.setDescription(dto.getDescription());
        return result;
    }
}
