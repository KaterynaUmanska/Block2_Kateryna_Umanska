package org.example.block2.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.block2.dto.MaterialDto;
import org.example.block2.exception.DuplicateResourceException;
import org.example.block2.exception.ResourceNotFoundException;
import org.example.block2.mapper.MaterialMapper;
import org.example.block2.monitor.Monitored;
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
    private final MaterialMapper materialMapper;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Creates a new material.
     *
     * @param dto material save DTO
     * @return created material DTO
     * @throws DuplicateResourceException if duplicate name or invalid data
     */
    @Transactional
    @Monitored
    public MaterialDto saveMaterial(MaterialSaveDto dto) {
        log.info("Attempting to save new material with name: '{}'", dto.getName());

        if (materialRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new DuplicateResourceException("Material with name '%s' already exists".formatted(dto.getName()));
        }

        MaterialData material = materialMapper.toEntity(dto);
        MaterialData saved = materialRepository.save(material);

        log.info("Successfully saved material with ID: {}", saved.getId());
        return materialMapper.toDto(saved);
    }

    /**
     * Returns all materials as DTOs.
     *
     * @return list of material DTOs
     */
    @Monitored
    @Transactional(readOnly = true)
    public List<MaterialDto> getAllMaterials() {
        log.debug("Fetching all materials from database");

        List<MaterialDto> materials = materialRepository.findAll().stream()
                .map(materialMapper::toDto)
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
    @Monitored
    @Transactional(readOnly = true)
    public MaterialDto getMaterialById(Long id) {
        log.debug("Fetching material by ID: {}", id);

        MaterialData data = materialRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Material with ID {} not found", id);
                    return new ResourceNotFoundException("Material not found with id: " + id);
                });

        return materialMapper.toDto(data);
    }

    /**
     * Updates an existing material.
     *
     * @param id  material ID
     * @param dto new material data
     */
    @Monitored
    @Transactional
    public void updateMaterial(Long id, MaterialSaveDto dto) {
        log.info("Attempting to update material with ID: {}", id);

        MaterialData material = materialRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot upload material, material with ID {} not found", id);
                    return new ResourceNotFoundException("Material not found with id: " + id);
                });

        materialRepository.findByNameIgnoreCase(dto.getName())
                .ifPresent(existing -> {
                   if (!existing.getId().equals(id)) {
                        throw new DuplicateResourceException("Material with name '%s' already exists".formatted(dto.getName()));
                    }
                });

        material.setName(dto.getName());
        material.setUnit(dto.getUnit());
        material.setDescription(dto.getDescription());

        materialRepository.save(material);
        log.info("Successfully updated material with ID: {}", id);
    }

    /**
     * Deletes material by ID.
     *
     * @param id material ID
     */
    @Monitored
    @Transactional
    public void deleteMaterial(Long id) {
        log.info("Attempting to delete material with ID: {}", id);

        MaterialData material = materialRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot delete material, material with ID {} not found", id);
                    return new ResourceNotFoundException("Material not found with id: " + id);
                });

        materialRepository.delete(material);
        entityManager.flush();
        log.info("Successfully deleted material with ID: {}", id);
    }

}
