package org.example.block2.service;

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

    @Transactional
    public MaterialData saveMaterial(MaterialSaveDto dto) {
        log.info("Attempting to save new material with name: '{}'", dto.getName());

        MaterialData material = new MaterialData();
        material.setName(dto.getName());
        material.setDescription(dto.getDescription());
        material.setUnit(dto.getUnit());

        MaterialData savedMaterial = materialRepository.save(material);
        log.info("Successfully saved material with ID: {}", savedMaterial.getId());

        return savedMaterial;
    }

    @Transactional(readOnly = true)
    public List<MaterialData> getAllMaterials() {
        log.debug("Fetching all materials from the database");
        List<MaterialData> materials = materialRepository.findAll();
        log.info("Found {} materials in total", materials.size());

        return materials;
    }

    @Transactional(readOnly = true)
    public MaterialData getMaterialById(Long id) {
        log.debug("Fetching material by ID: {}", id);
        return materialRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Material with ID {} not found", id);
                    return new IllegalArgumentException("Material not found with id: " + id);
                });
    }
}
