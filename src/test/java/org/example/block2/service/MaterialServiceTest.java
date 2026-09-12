package org.example.block2.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.example.block2.data.MaterialData;
import org.example.block2.dict.Unit;
import org.example.block2.dto.MaterialDto;
import org.example.block2.dto.MaterialSaveDto;
import org.example.block2.exception.DuplicateResourceException;
import org.example.block2.exception.ResourceNotFoundException;
import org.example.block2.repository.MaterialRepository;
import org.example.block2.repository.PurchaseRecordRepository;
import org.example.block2.utils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
public class MaterialServiceTest {

    @Autowired
    private MaterialService materialService;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    PurchaseRecordRepository purchaseRecordRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void tearDown() {
        purchaseRecordRepository.deleteAll();
        materialRepository.deleteAll();
    }

    @Transactional
    @Test
    void saveMaterial_shouldSaveAndReturnDto() {
        MaterialSaveDto request = MaterialSaveDto.builder()
                .name("Steel beam")
                .description("Construction steel")
                .unit(Unit.KG)
                .build();

        MaterialDto result = materialService.saveMaterial(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Steel beam");
        assertThat(result.getDescription()).isEqualTo("Construction steel");
        assertThat(result.getUnit()).isEqualTo(Unit.KG);

        MaterialData entity = entityManager.find(MaterialData.class, result.getId());
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Steel beam");
    }

    @Test
    void saveMaterial_whenNameExists_shouldThrowDuplicateException() {
        testDataFactory.createMaterial("Steel beam");

        MaterialSaveDto request = MaterialSaveDto.builder()
                .name("Steel beam")
                .description("Another steel")
                .unit(Unit.KG)
                .build();

        assertThatThrownBy(() -> materialService.saveMaterial(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void saveMaterial_whenNameExistsCaseInsensitive_shouldThrowDuplicateException() {
        testDataFactory.createMaterial("Steel Beam");

        MaterialSaveDto request = MaterialSaveDto.builder()
                .name("steel beam")
                .description("Another steel")
                .unit(Unit.KG)
                .build();

        assertThatThrownBy(() -> materialService.saveMaterial(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @Transactional
    void getAllMaterials_shouldReturnAllMaterials() {
        testDataFactory.createMaterial("Material 1");
        testDataFactory.createMaterial("Material 2");

        List<MaterialDto> materials = materialService.getAllMaterials();

        assertThat(materials).hasSize(2);
        assertThat(materials).extracting(MaterialDto::getName)
                .containsExactlyInAnyOrder("Material 1", "Material 2");
    }

    @Test
    @Transactional
    void getMaterialById_shouldReturnMaterial() {
        MaterialData saved = testDataFactory.createMaterial("Copper wire");

        MaterialDto result = materialService.getMaterialById(saved.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getName()).isEqualTo("Copper wire");
        assertThat(result.getUnit()).isEqualTo(Unit.KG);
    }

    @Test
    void getMaterialById_whenNotFound_shouldThrowNotFoundException() {
        assertThatThrownBy(() -> materialService.getMaterialById(999999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Material not found with id: 999999");
    }

    @Test
    @Transactional
    void updateMaterial_shouldUpdateFields() {
        MaterialData saved = testDataFactory.createMaterial("Old Name");

        MaterialSaveDto updateRequest = MaterialSaveDto.builder()
                .name("New Name")
                .description("Updated description")
                .unit(Unit.METERS)
                .build();

        materialService.updateMaterial(saved.getId(), updateRequest);

        entityManager.flush();
        entityManager.clear();

        MaterialData updated = entityManager.find(MaterialData.class, saved.getId());
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getUnit()).isEqualTo(Unit.METERS);
    }

    @Test
    @Transactional
    void updateMaterial_withSameName_shouldSucceed() {
        MaterialData saved = testDataFactory.createMaterial("Same Name");

        MaterialSaveDto updateRequest = MaterialSaveDto.builder()
                .name("Same Name")
                .description("Updated description")
                .unit(Unit.KG)
                .build();

        materialService.updateMaterial(saved.getId(), updateRequest);

        MaterialData updated = entityManager.find(MaterialData.class, saved.getId());
        assertThat(updated.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void updateMaterial_withExistingNameOfAnotherMaterial_shouldThrowDuplicateException() {
        testDataFactory.createMaterial("Material A");
        MaterialData materialB = testDataFactory.createMaterial("Material B");

        MaterialSaveDto updateRequest = MaterialSaveDto.builder()
                .name("Material A")
                .description("Conflict")
                .unit(Unit.KG)
                .build();

        assertThatThrownBy(() -> materialService.updateMaterial(materialB.getId(), updateRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void updateMaterial_whenNotFound_shouldThrowNotFoundException() {
        MaterialSaveDto updateRequest = MaterialSaveDto.builder()
                .name("Name")
                .unit(Unit.KG)
                .build();

        assertThatThrownBy(() -> materialService.updateMaterial(999999L, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Material not found with id: 999999");
    }

    @Test
    @Transactional
    void deleteMaterial_shouldRemoveMaterial() {
        MaterialData saved = testDataFactory.createMaterial("To delete");

        materialService.deleteMaterial(saved.getId());

        entityManager.flush();
        entityManager.clear();

        MaterialData deleted = entityManager.find(MaterialData.class, saved.getId());
        assertThat(deleted).isNull();
    }

    @Test
    void deleteMaterial_whenNotFound_shouldThrowNotFoundException() {
        assertThatThrownBy(() -> materialService.deleteMaterial(999999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Material not found with id: 999999");
    }
}