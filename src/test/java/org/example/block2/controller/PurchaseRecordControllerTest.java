package org.example.block2.controller;

import org.example.block2.data.MaterialData;
import org.example.block2.data.PurchaseRecordData;
import org.example.block2.dict.Unit;
import org.example.block2.dto.PurchaseRecordSaveDto;
import org.example.block2.repository.MaterialRepository;
import org.example.block2.repository.PurchaseRecordRepository;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.notNullValue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PurchaseRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PurchaseRecordRepository purchaseRecordRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @AfterEach
    void tearDown() {
        purchaseRecordRepository.deleteAll();
        materialRepository.deleteAll();
    }

    private MaterialData createMaterial() {
        return createMaterial("Test steel");
    }

    private MaterialData createMaterial(String name) {
        MaterialData material = new MaterialData();
        material.setName(name);
        material.setDescription("Test material");
        material.setUnit(Unit.KG);

        return materialRepository.save(material);
    }
    private PurchaseRecordData createPurchaseRecord(
            Long orderId,
            MaterialData material,
            Double quantity
    ) {
        PurchaseRecordData record = new PurchaseRecordData();
        record.setOrderId(orderId);
        record.setMaterial(material);
        record.setQuantity(BigDecimal.valueOf(quantity));

        return purchaseRecordRepository.save(record);
    }

    @Test
    void createPurchaseRecord_shouldReturnCreatedId() throws Exception {
        MaterialData material = createMaterial();

        PurchaseRecordSaveDto request = PurchaseRecordSaveDto.builder()
                .orderId(100L)
                .materialId(material.getId())
                .quantity(BigDecimal.valueOf(25.5))
                .build();

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void createPurchaseRecord_withMinimalData_shouldReturnCreatedId() throws Exception {
        MaterialData material = createMaterial();

        PurchaseRecordSaveDto request = PurchaseRecordSaveDto.builder()
                .orderId(200L)
                .materialId(material.getId())
                .quantity(BigDecimal.valueOf(10.0))
                .build();

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void createPurchaseRecord_withoutOrderId_shouldReturnBadRequest() throws Exception {
        MaterialData material = createMaterial();

        PurchaseRecordSaveDto request = PurchaseRecordSaveDto.builder()
                .materialId(material.getId())
                .quantity(BigDecimal.valueOf(10.0))
                .build();

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPurchaseRecord_withoutMaterialId_shouldReturnBadRequest() throws Exception {
        PurchaseRecordSaveDto request = PurchaseRecordSaveDto.builder()
                .orderId(100L)
                .quantity(BigDecimal.valueOf(10.0))
                .build();

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPurchaseRecord_withoutQuantity_shouldReturnBadRequest() throws Exception {
        MaterialData material = createMaterial();

        PurchaseRecordSaveDto request = PurchaseRecordSaveDto.builder()
                .orderId(100L)
                .materialId(material.getId())
                .build();

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPurchaseRecord_withInvalidQuantity_shouldReturnBadRequest() throws Exception {
        MaterialData material = createMaterial();

        PurchaseRecordSaveDto request = PurchaseRecordSaveDto.builder()
                .orderId(100L)
                .materialId(material.getId())
                .quantity(BigDecimal.valueOf(0.0))
                .build();

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPurchaseRecord_withNonExistingMaterial_shouldReturnNotFound() throws Exception {
        PurchaseRecordSaveDto request = PurchaseRecordSaveDto.builder()
                .orderId(100L)
                .materialId(999999L)
                .quantity(BigDecimal.valueOf(10.0))
                .build();

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPurchaseRecord_withDuplicateOrderAndMaterial_shouldReturnConflict() throws Exception {
        MaterialData material = createMaterial();

        PurchaseRecordSaveDto request = PurchaseRecordSaveDto.builder()
                .orderId(100L)
                .materialId(material.getId())
                .quantity(BigDecimal.valueOf(10.0))
                .build();

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getPurchaseRecord_shouldReturnPurchaseRecord() throws Exception {
        MaterialData material = createMaterial();

        PurchaseRecordData purchaseRecord = new PurchaseRecordData();
        purchaseRecord.setOrderId(100L);
        purchaseRecord.setMaterial(material);
        purchaseRecord.setQuantity(BigDecimal.valueOf(25.5));

        PurchaseRecordData saved = purchaseRecordRepository.save(purchaseRecord);

        mockMvc.perform(get("/api/purchases/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.orderId").value(100))
                .andExpect(jsonPath("$.quantity").value(25.5))
                .andExpect(jsonPath("$.material.id").value(material.getId()))
                .andExpect(jsonPath("$.material.name").value("Test steel"))
                .andExpect(jsonPath("$.material.unit").value("KG"));
    }

    @Test
    void getPurchaseRecord_whenNotFound_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/purchases/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePurchaseRecord_shouldReturnNoContent() throws Exception {
        MaterialData material = createMaterial();

        PurchaseRecordData purchaseRecord = new PurchaseRecordData();
        purchaseRecord.setOrderId(100L);
        purchaseRecord.setMaterial(material);
        purchaseRecord.setQuantity(BigDecimal.valueOf(10.0));

        PurchaseRecordData saved = purchaseRecordRepository.save(purchaseRecord);

        PurchaseRecordSaveDto request = PurchaseRecordSaveDto.builder()
                .orderId(200L)
                .materialId(material.getId())
                .quantity(BigDecimal.valueOf(50.0))
                .build();

        mockMvc.perform(put("/api/purchases/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        PurchaseRecordData updated = purchaseRecordRepository
                .findById(saved.getId())
                .orElseThrow();

        assertEquals(200L, updated.getOrderId());
        assertEquals(0, BigDecimal.valueOf(50.0).compareTo(updated.getQuantity()));
        assertEquals(material.getId(), updated.getMaterial().getId());
    }

    @Test
    void updatePurchaseRecord_shouldChangeMaterial() throws Exception {
        MaterialData firstMaterial = createMaterial();

        MaterialData secondMaterial = new MaterialData();
        secondMaterial.setName("Test cable");
        secondMaterial.setDescription("Test cable material");
        secondMaterial.setUnit(Unit.METERS);

        secondMaterial = materialRepository.save(secondMaterial);

        PurchaseRecordData purchaseRecord = new PurchaseRecordData();
        purchaseRecord.setOrderId(100L);
        purchaseRecord.setMaterial(firstMaterial);
        purchaseRecord.setQuantity(BigDecimal.valueOf(10.0));

        PurchaseRecordData saved = purchaseRecordRepository.save(purchaseRecord);

        PurchaseRecordSaveDto request = PurchaseRecordSaveDto.builder()
                .orderId(100L)
                .materialId(secondMaterial.getId())
                .quantity(BigDecimal.valueOf(20.0))
                .build();

        mockMvc.perform(put("/api/purchases/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        PurchaseRecordData updated = purchaseRecordRepository
                .findById(saved.getId())
                .orElseThrow();

        assertEquals(secondMaterial.getId(), updated.getMaterial().getId());
        assertEquals(0, BigDecimal.valueOf(20.0).compareTo(updated.getQuantity()));
    }

    @Test
    void updatePurchaseRecord_whenNotFound_shouldReturnNotFound() throws Exception {
        MaterialData material = createMaterial();

        PurchaseRecordSaveDto request = PurchaseRecordSaveDto.builder()
                .orderId(100L)
                .materialId(material.getId())
                .quantity(BigDecimal.valueOf(20.0))
                .build();

        mockMvc.perform(put("/api/purchases/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePurchaseRecord_withInvalidQuantity_shouldReturnBadRequest() throws Exception {
        MaterialData material = createMaterial();

        PurchaseRecordData purchaseRecord = new PurchaseRecordData();
        purchaseRecord.setOrderId(100L);
        purchaseRecord.setMaterial(material);
        purchaseRecord.setQuantity(BigDecimal.valueOf(10.0));

        PurchaseRecordData saved = purchaseRecordRepository.save(purchaseRecord);

        PurchaseRecordSaveDto request = PurchaseRecordSaveDto.builder()
                .orderId(200L)
                .materialId(material.getId())
                .quantity(BigDecimal.valueOf(-5.0))
                .build();

        mockMvc.perform(put("/api/purchases/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletePurchaseRecord_shouldReturnNoContent() throws Exception {
        MaterialData material = createMaterial();

        PurchaseRecordData purchaseRecord = new PurchaseRecordData();
        purchaseRecord.setOrderId(100L);
        purchaseRecord.setMaterial(material);
        purchaseRecord.setQuantity(BigDecimal.valueOf(10.0));

        PurchaseRecordData saved = purchaseRecordRepository.save(purchaseRecord);

        mockMvc.perform(delete("/api/purchases/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        assertFalse(purchaseRecordRepository.existsById(saved.getId()));
    }

    @Test
    void deletePurchaseRecord_whenNotFound_shouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/purchases/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPurchaseRecords_shouldReturnPaginatedRecords() throws Exception {
        MaterialData steel = createMaterial();

        createPurchaseRecord(100L, steel, 10.0);
        createPurchaseRecord(101L, steel, 20.0);
        createPurchaseRecord(102L, steel, 30.0);

        String request = """
            {
                "page": 1,
                "size": 2
            }
            """;

        mockMvc.perform(post("/api/purchases/_list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.list.length()").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }
    @Test
    void getPurchaseRecords_withOrderIdFilter_shouldReturnMatchingRecords() throws Exception {
        MaterialData material = createMaterial();
        MaterialData material2 = createMaterial("Wood");

        createPurchaseRecord(100L, material, 10.0);
        createPurchaseRecord(200L, material, 20.0);
        createPurchaseRecord(100L, material2, 30.0);

        String request = """
            {
                "orderId": 100,
                "page": 1,
                "size": 20
            }
            """;

        mockMvc.perform(post("/api/purchases/_list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(2))
                .andExpect(jsonPath("$.list[0].orderId").value(100))
                .andExpect(jsonPath("$.list[1].orderId").value(100));
    }
    @Test
    void getPurchaseRecords_withMaterialNameFilter_shouldReturnMatchingRecords() throws Exception {
        MaterialData steel = createMaterial();

        MaterialData cable = new MaterialData();
        cable.setName("Test cable");
        cable.setDescription("Test cable");
        cable.setUnit(Unit.METERS);
        cable = materialRepository.save(cable);

        createPurchaseRecord(100L, steel, 10.0);
        createPurchaseRecord(101L, cable, 20.0);
        createPurchaseRecord(102L, cable, 30.0);

        String request = """
            {
                "materialName": "Test cable",
                "page": 1,
                "size": 20
            }
            """;

        mockMvc.perform(post("/api/purchases/_list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(2))
                .andExpect(jsonPath("$.list[0].materialName").value("Test cable"))
                .andExpect(jsonPath("$.list[1].materialName").value("Test cable"));
    }
    @Test
    void getPurchaseRecords_withQuantityRange_shouldReturnMatchingRecords() throws Exception {
        MaterialData material = createMaterial();

        createPurchaseRecord(100L, material, 5.0);
        createPurchaseRecord(101L, material, 15.0);
        createPurchaseRecord(102L, material, 25.0);
        createPurchaseRecord(103L, material, 35.0);

        String request = """
            {
                "quantityFrom": 10,
                "quantityTo": 30,
                "page": 1,
                "size": 20
            }
            """;

        mockMvc.perform(post("/api/purchases/_list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(2))
                .andExpect(jsonPath("$.list[0].quantity").value(15.0))
                .andExpect(jsonPath("$.list[1].quantity").value(25.0));
    }
    @Test
    void getPurchaseRecords_withMultipleFilters_shouldReturnMatchingRecords() throws Exception {
        MaterialData material = createMaterial();
        MaterialData material2 = createMaterial("Wood");

        createPurchaseRecord(100L, material, 10.0);
        createPurchaseRecord(200L, material, 20.0);
        createPurchaseRecord(300L, material, 50.0);
        createPurchaseRecord(400L, material2, 20.0);

        String request = """
            {
                "materialName": "Test steel",
                "quantityFrom": 15,
                "quantityTo": 25,
                "page": 1,
                "size": 20
            }
            """;

        mockMvc.perform(post("/api/purchases/_list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(1))
                .andExpect(jsonPath("$.list[0].orderId").value(200))
                .andExpect(jsonPath("$.list[0].quantity").value(20.0));
    }
    @Test
    void generateReport_shouldReturnCsvFile() throws Exception {
        MaterialData material = createMaterial();

        createPurchaseRecord(100L, material, 10.0);
        createPurchaseRecord(101L, material, 20.0);

        String request = """
            {
                "page": 1,
                "size": 20
            }
            """;

        mockMvc.perform(post("/api/purchases/_report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Content-Type",
                        org.hamcrest.Matchers.containsString("text/csv") // Проверяем, что тип содержит text/csv (игнорируя charset)
                ))
                .andExpect(header().string(
                        "Content-Disposition",
                        org.hamcrest.Matchers.containsString("attachment")
                ))
                .andExpect(header().string(
                        "Content-Disposition",
                        org.hamcrest.Matchers.containsString(".csv")
                ))
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("100")
                ))
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("10,0")
                ));
    }
    @Test
    void generateReport_withOrderIdFilter_shouldReturnOnlyMatchingRecords() throws Exception {
        MaterialData material = createMaterial();

        createPurchaseRecord(100L, material, 10.0);
        createPurchaseRecord(200L, material, 20.0);

        String request = """
            {
                "orderId": 100,
                "page": 1,
                "size": 20
            }
            """;

        mockMvc.perform(post("/api/purchases/_report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("100")
                ))
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("10,0")
                ));
    }

    @Test
    void upload_shouldImportValidRecords() throws Exception {
        MaterialData material = createMaterial();

        String json = """
        [
            {
                "orderId": 100,
                "materialId": %d,
                "quantity": 10.5
            },
            {
                "orderId": 101,
                "materialId": %d,
                "quantity": 20.0
            }
        ]
        """.formatted(material.getId(), material.getId());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "purchases.json",
                MediaType.APPLICATION_JSON_VALUE,
                json.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(
                        multipart("/api/purchases/upload")
                                .file(file)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value(2))
                .andExpect(jsonPath("$.failed").value(0));

        assertEquals(
                2,
                purchaseRecordRepository.count()
        );
    }

    @Test
    void upload_withInvalidRecord_shouldCountFailedRecords() throws Exception {
        MaterialData material = createMaterial();

        String json = """
    [
        {
            "orderId": 100,
            "materialId": %d,
            "quantity": 10.5
        },
        {
            "orderId": 101,
            "materialId": %d,
            "quantity": -5.0
        }
    ]
    """.formatted(material.getId(), material.getId());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "purchases.json",
                MediaType.APPLICATION_JSON_VALUE,
                json.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(
                        multipart("/api/purchases/upload")
                                .file(file)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value(1))
                .andExpect(jsonPath("$.failed").value(1));

        assertEquals(
                1,
                purchaseRecordRepository.count()
        );
    }

    @Test
    void upload_withNonExistingMaterial_shouldCountAsFailed() throws Exception {
        String json = """
        [
            {
                "orderId": 100,
                "materialId": 999999,
                "quantity": 10.5
            }
        ]
        """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "purchases.json",
                MediaType.APPLICATION_JSON_VALUE,
                json.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(
                        multipart("/api/purchases/upload")
                                .file(file)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value(0))
                .andExpect(jsonPath("$.failed").value(1));

        assertEquals(
                0,
                purchaseRecordRepository.count()
        );
    }

    @Test
    void upload_withDuplicateRecords_shouldPartiallyImport() throws Exception {
        MaterialData material = createMaterial();

        String json = """
        [
            {
                "orderId": 100,
                "materialId": %d,
                "quantity": 10.5
            },
            {
                "orderId": 100,
                "materialId": %d,
                "quantity": 20.0
            }
        ]
        """.formatted(material.getId(), material.getId());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "purchases.json",
                MediaType.APPLICATION_JSON_VALUE,
                json.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(
                        multipart("/api/purchases/upload")
                                .file(file)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value(1))
                .andExpect(jsonPath("$.failed").value(1));

        assertEquals(
                1,
                purchaseRecordRepository.count()
        );
    }
}
