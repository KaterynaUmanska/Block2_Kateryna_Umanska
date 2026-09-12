package org.example.block2.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.example.block2.data.MaterialData;
import org.example.block2.data.PurchaseRecordData;
import org.example.block2.dict.Unit;
import org.example.block2.dto.*;
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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class PurchaseRecordServiceTest {

    @Autowired
    PurchaseRecordService purchaseRecordService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    private PurchaseRecordRepository purchaseRecordRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void tearDown() {
        purchaseRecordRepository.deleteAll();
        materialRepository.deleteAll();
    }

    @Transactional
    @Test
    void createPurchaseRecord() {
        MaterialData material = testDataFactory.createMaterial("Test name");

        PurchaseRecordDto result = purchaseRecordService.savePurchaseRecord(
                PurchaseRecordSaveDto.builder()
                        .orderId(100L)
                        .materialId(material.getId())
                        .quantity(BigDecimal.valueOf(50.0))
                        .build()
        );

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(100L);
        assertThat(result.getQuantity()).isEqualByComparingTo("50.0");
        assertThat(result.getMaterial()).isNotNull();
        assertThat(result.getMaterial().getId()).isEqualTo(material.getId());
        assertThat(result.getMaterial().getName()).isEqualTo("Test name");

        PurchaseRecordData record =
                entityManager.find(PurchaseRecordData.class, result.getId());

        assertThat(record).isNotNull();
        assertThat(record.getOrderId()).isEqualTo(100L);
        assertThat(record.getQuantity()).isEqualByComparingTo("50.0");
        assertThat(record.getMaterial()).isNotNull();
        assertThat(record.getMaterial().getName()).isEqualTo("Test name");
    }

    @Test
    @Transactional
    void getPurchaseRecordById() {
        MaterialData material = testDataFactory.createMaterial("Test name");
        PurchaseRecordData created = testDataFactory.createPurchaseRecord(100L, material, 50.0);

        PurchaseRecordDto result =
                purchaseRecordService.getPurchaseRecordById(created.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getOrderId()).isEqualTo(100L);
        assertThat(result.getQuantity()).isEqualByComparingTo("50.0");

        assertThat(result.getMaterial()).isNotNull();
        assertThat(result.getMaterial().getId()).isEqualTo(material.getId());
        assertThat(result.getMaterial().getName()).isEqualTo("Test name");
    }

    @Test
    void getPurchaseRecordById_notFound() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> purchaseRecordService.getPurchaseRecordById(999999L)
                )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Purchase record not found");
    }

    @Test
    @Transactional
    void updatePurchaseRecord() {
        MaterialData material1 = testDataFactory.createMaterial("Steel");

        MaterialData material2 = testDataFactory.createMaterial("Cable");
        material2.setUnit(Unit.METERS);
        material2 = materialRepository.save(material2);

        PurchaseRecordData created = testDataFactory.createPurchaseRecord(100L, material1, 10.0);

        purchaseRecordService.updatePurchaseRecord(
                created.getId(),
                PurchaseRecordSaveDto.builder()
                        .orderId(200L)
                        .materialId(material2.getId())
                        .quantity(BigDecimal.valueOf(25.0))
                        .build()
        );

        entityManager.flush();
        entityManager.clear();

        PurchaseRecordData record =
                entityManager.find(PurchaseRecordData.class, created.getId());

        assertThat(record).isNotNull();
        assertThat(record.getOrderId()).isEqualTo(200L);
        assertThat(record.getQuantity()).isEqualByComparingTo("25.0");
        assertThat(record.getMaterial()).isNotNull();
        assertThat(record.getMaterial().getId()).isEqualTo(material2.getId());
        assertThat(record.getMaterial().getName()).isEqualTo("Cable");
    }

    @Test
    void updatePurchaseRecord_notFound() {
        MaterialData material = testDataFactory.createMaterial("Test name");

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> purchaseRecordService.updatePurchaseRecord(
                                999999L,
                                PurchaseRecordSaveDto.builder()
                                        .orderId(100L)
                                        .materialId(material.getId())
                                        .quantity(BigDecimal.valueOf(10.0))
                                        .build()
                        )
                )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Purchase record not found");
    }

    @Test
    @Transactional
    void deletePurchaseRecord() {
        MaterialData material = testDataFactory.createMaterial("Test name");
        PurchaseRecordData created = testDataFactory.createPurchaseRecord(100L, material, 50.0);

        Long id = created.getId();

        purchaseRecordService.deletePurchaseRecord(id);

        entityManager.flush();
        entityManager.clear();

        PurchaseRecordData record =
                entityManager.find(PurchaseRecordData.class, id);

        assertThat(record).isNull();
    }

    @Test
    void deletePurchaseRecord_notFound() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> purchaseRecordService.deletePurchaseRecord(999999L)
                )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Purchase record not found");
    }

    @Test
    void createPurchaseRecord_materialNotFound() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> purchaseRecordService.savePurchaseRecord(
                                PurchaseRecordSaveDto.builder()
                                        .orderId(100L)
                                        .materialId(999999L)
                                        .quantity(BigDecimal.valueOf(50.0))
                                        .build()
                        )
                )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Material not found");
    }

    @Test
    void createPurchaseRecord_duplicate() {
        MaterialData material = testDataFactory.createMaterial("Test name");

        PurchaseRecordSaveDto dto = PurchaseRecordSaveDto.builder()
                .orderId(100L)
                .materialId(material.getId())
                .quantity(BigDecimal.valueOf(50.0))
                .build();

        purchaseRecordService.savePurchaseRecord(dto);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> purchaseRecordService.savePurchaseRecord(dto)
                )
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @Transactional
    void getPurchaseRecords_withFiltersAndPagination() {
        MaterialData steel = testDataFactory.createMaterial("Steel");
        MaterialData cable = testDataFactory.createMaterial("Cable");

        testDataFactory.createPurchaseRecord(100L, steel, 10.0);
        testDataFactory.createPurchaseRecord(100L, cable, 20.0);
        testDataFactory.createPurchaseRecord(200L, cable, 30.0);

        PurchaseRecordListFilterDto filter = new PurchaseRecordListFilterDto();
        filter.setOrderId(100L);
        filter.setMaterialName("Cable");
        filter.setQuantityFrom(BigDecimal.valueOf(15.0));
        filter.setQuantityTo(BigDecimal.valueOf(25.0));
        filter.setPage(1);
        filter.setSize(10);

        PurchaseRecordListResponse result =
                purchaseRecordService.getPurchaseRecords(filter);

        assertThat(result).isNotNull();
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getTotalPages()).isEqualTo(1);

        PurchaseRecordListDto record = result.getList().get(0);

        assertThat(record.getOrderId()).isEqualTo(100L);
        assertThat(record.getMaterialName()).isEqualTo("Cable");
        assertThat(record.getQuantity()).isEqualByComparingTo("20.0");
    }

    @Test
    @Transactional
    void generateReport() {
        String uniqueMaterialName = "Test name " + java.util.UUID.randomUUID();
        MaterialData material = testDataFactory.createMaterial(uniqueMaterialName);

        testDataFactory.createPurchaseRecord(100L, material, 20.0);

        PurchaseRecordFilterDto filter = new PurchaseRecordFilterDto();
        filter.setOrderId(100L);
        filter.setMaterialName(uniqueMaterialName);
        filter.setQuantityFrom(BigDecimal.valueOf(15.0));
        filter.setQuantityTo(BigDecimal.valueOf(25.0));

        byte[] report = purchaseRecordService.generateReport(filter);

        assertThat(report).isNotNull();
        assertThat(report).isNotEmpty();

        String csv = new String(report, java.nio.charset.StandardCharsets.UTF_8);

        assertThat(csv).contains("100");
        assertThat(csv).contains("20,0");
        assertThat(csv).contains(uniqueMaterialName);
    }

    @Test
    void importPurchaseRecords() throws Exception {
        MaterialData material = testDataFactory.createMaterial("Test name");

        String json = """
            [
              {
                "orderId": 100,
                "materialId": %d,
                "quantity": 10.0
              },
              {
                "orderId": 200,
                "materialId": %d,
                "quantity": 20.0
              }
            ]
            """.formatted(material.getId(), material.getId());

        java.io.ByteArrayInputStream inputStream =
                new java.io.ByteArrayInputStream(
                        json.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                );

        PurchaseRecordProcessingResult result =
                purchaseRecordService.importPurchaseRecords(inputStream);

        assertThat(result).isNotNull();
        assertThat(result.getSuccessful()).isEqualTo(2);
        assertThat(result.getFailed()).isEqualTo(0);

        PurchaseRecordListFilterDto filter = new PurchaseRecordListFilterDto();
        PurchaseRecordListResponse response = purchaseRecordService.getPurchaseRecords(filter);

        assertThat(response).isNotNull();
        assertThat(response.getList()).hasSize(2);
    }
}