package org.example.block2.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.example.block2.data.PurchaseRecordData;
import org.example.block2.dict.Unit;
import org.example.block2.dto.*;
import org.example.block2.exception.DuplicateResourceException;
import org.example.block2.exception.ResourceNotFoundException;
import org.example.block2.repository.MaterialRepository;
import org.example.block2.repository.PurchaseRecordRepository;
import org.junit.jupiter.api.AfterEach;
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
    MaterialService materialService;

    @Autowired
    PurchaseRecordService purchaseRecordService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    private PurchaseRecordRepository purchaseRecordRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @AfterEach
    void tearDown() {
        purchaseRecordRepository.deleteAll();
        materialRepository.deleteAll();
    }

    @Test
    @Transactional
    void createPurchaseRecord() {

        MaterialDto material = materialService.saveMaterial(
                MaterialSaveDto.builder()
                        .name("Test name")
                        .description("Test material")
                        .unit(Unit.KG)
                        .build()
        );

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
        assertThat(result.getQuantity()).isEqualTo(50.0);
        assertThat(result.getMaterial()).isNotNull();
        assertThat(result.getMaterial().getId()).isEqualTo(material.getId());
        assertThat(result.getMaterial().getName()).isEqualTo("Test name");

        PurchaseRecordData record =
                entityManager.find(PurchaseRecordData.class, result.getId());

        assertThat(record).isNotNull();
        assertThat(record.getOrderId()).isEqualTo(100L);
        assertThat(record.getQuantity()).isEqualTo(50.0);
        assertThat(record.getMaterial()).isNotNull();
        assertThat(record.getMaterial().getName()).isEqualTo("Test name");
    }

    @Test
    @Transactional
    void getPurchaseRecordById() {

        MaterialDto material = materialService.saveMaterial(
                MaterialSaveDto.builder()
                        .name("Test name")
                        .description("Test material")
                        .unit(Unit.KG)
                        .build()
        );

        PurchaseRecordDto created = purchaseRecordService.savePurchaseRecord(
                PurchaseRecordSaveDto.builder()
                        .orderId(100L)
                        .materialId(material.getId())
                        .quantity(BigDecimal.valueOf(50.0))
                        .build()
        );

        PurchaseRecordDto result =
                purchaseRecordService.getPurchaseRecordById(created.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getOrderId()).isEqualTo(100L);
        assertThat(result.getQuantity()).isEqualTo(50.0);

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

        MaterialDto material1 = materialService.saveMaterial(
                MaterialSaveDto.builder()
                        .name("Steel")
                        .description("Steel material")
                        .unit(Unit.KG)
                        .build()
        );

        MaterialDto material2 = materialService.saveMaterial(
                MaterialSaveDto.builder()
                        .name("Cable")
                        .description("Copper cable")
                        .unit(Unit.METERS)
                        .build()
        );

        PurchaseRecordDto created = purchaseRecordService.savePurchaseRecord(
                PurchaseRecordSaveDto.builder()
                        .orderId(100L)
                        .materialId(material1.getId())
                        .quantity(BigDecimal.valueOf(10.0))
                        .build()
        );

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
        assertThat(record.getQuantity()).isEqualTo(25.0);
        assertThat(record.getMaterial()).isNotNull();
        assertThat(record.getMaterial().getId()).isEqualTo(material2.getId());
        assertThat(record.getMaterial().getName()).isEqualTo("Cable");
    }

    @Test
    void updatePurchaseRecord_notFound() {

        MaterialDto material = materialService.saveMaterial(
                MaterialSaveDto.builder()
                        .name("Test name")
                        .description("Test material")
                        .unit(Unit.KG)
                        .build()
        );

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

        MaterialDto material = materialService.saveMaterial(
                MaterialSaveDto.builder()
                        .name("Test name")
                        .description("Test material")
                        .unit(Unit.KG)
                        .build()
        );

        PurchaseRecordDto created = purchaseRecordService.savePurchaseRecord(
                PurchaseRecordSaveDto.builder()
                        .orderId(100L)
                        .materialId(material.getId())
                        .quantity(BigDecimal.valueOf(50.0))
                        .build()
        );

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
    @Transactional
    void createPurchaseRecord_duplicate() {

        MaterialDto material = materialService.saveMaterial(
                MaterialSaveDto.builder()
                        .name("Test name")
                        .description("Test material")
                        .unit(Unit.KG)
                        .build()
        );

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

        MaterialDto steel = materialService.saveMaterial(
                MaterialSaveDto.builder()
                        .name("Steel")
                        .description("Steel")
                        .unit(Unit.KG)
                        .build()
        );

        MaterialDto cable = materialService.saveMaterial(
                MaterialSaveDto.builder()
                        .name("Cable")
                        .description("Cable")
                        .unit(Unit.METERS)
                        .build()
        );

        purchaseRecordService.savePurchaseRecord(
                PurchaseRecordSaveDto.builder()
                        .orderId(100L)
                        .materialId(steel.getId())
                        .quantity(BigDecimal.valueOf(10.0))
                        .build()
        );

        purchaseRecordService.savePurchaseRecord(
                PurchaseRecordSaveDto.builder()
                        .orderId(100L)
                        .materialId(cable.getId())
                        .quantity(BigDecimal.valueOf(20.0))
                        .build()
        );

        purchaseRecordService.savePurchaseRecord(
                PurchaseRecordSaveDto.builder()
                        .orderId(200L)
                        .materialId(cable.getId())
                        .quantity(BigDecimal.valueOf(30.0))
                        .build()
        );

        PurchaseRecordFilterDto filter = new PurchaseRecordFilterDto();

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
        assertThat(record.getQuantity()).isEqualTo(20.0);
    }

    @Test
    @Transactional
    void generateReport() {
        String uniqueMaterialName = "Test name " + java.util.UUID.randomUUID();

        MaterialDto material = materialService.saveMaterial(
                MaterialSaveDto.builder()
                        .name(uniqueMaterialName)
                        .description("Test material")
                        .unit(Unit.KG)
                        .build()
        );

        purchaseRecordService.savePurchaseRecord(
                PurchaseRecordSaveDto.builder()
                        .orderId(100L)
                        .materialId(material.getId())
                        .quantity(BigDecimal.valueOf(20.0))
                        .build()
        );

        PurchaseRecordReportFilterDto filter = new PurchaseRecordReportFilterDto();
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
    @Transactional
    void importPurchaseRecords() throws Exception {

        MaterialDto material = materialService.saveMaterial(
                MaterialSaveDto.builder()
                        .name("Test name")
                        .description("Test material")
                        .unit(Unit.KG)
                        .build()
        );

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

        assertThat(purchaseRecordService.getAllPurchaseRecords())
                .hasSize(2);
    }
}
