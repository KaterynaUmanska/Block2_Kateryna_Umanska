package org.example.block2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.block2.data.MaterialData;
import org.example.block2.dict.Unit;
import org.example.block2.dto.MaterialSaveDto;
import org.example.block2.repository.MaterialRepository;
import org.example.block2.repository.PurchaseRecordRepository;
import org.example.block2.utils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private PurchaseRecordRepository purchaseRecordRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void tearDown() {
        purchaseRecordRepository.deleteAll();
        materialRepository.deleteAll();
    }

    @Test
    void createMaterial_shouldReturnCreatedId() throws Exception {
        MaterialSaveDto request = MaterialSaveDto.builder()
                .name("Test steel")
                .description("Test material")
                .unit(Unit.KG)
                .build();

        mockMvc.perform(post("/api/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void getMaterial_shouldReturnMaterial() throws Exception {
        MaterialData saved = testDataFactory.createMaterial("Test steel");

        mockMvc.perform(get("/api/materials/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Test steel"))
                .andExpect(jsonPath("$.description").value("Test material"))
                .andExpect(jsonPath("$.unit").value("KG"));
    }

    @Test
    void getAllMaterials_shouldReturnAllMaterials() throws Exception {
        testDataFactory.createMaterial("Test_1");
        testDataFactory.createMaterial("Test_2");

        mockMvc.perform(get("/api/materials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Test_1"))
                .andExpect(jsonPath("$[1].name").value("Test_2"));
    }

    @Test
    void getMaterial_whenNotFound_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/materials/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void createMaterial_withoutName_shouldReturnBadRequest() throws Exception {
        MaterialSaveDto request = MaterialSaveDto.builder()
                .description("Test material")
                .unit(Unit.KG)
                .build();

        mockMvc.perform(post("/api/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMaterial_withoutUnit_shouldReturnBadRequest() throws Exception {
        MaterialSaveDto request = MaterialSaveDto.builder()
                .name("Test steel")
                .description("Test material")
                .build();

        mockMvc.perform(post("/api/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMaterial_withDuplicateName_shouldReturnConflict() throws Exception {
        MaterialSaveDto request = MaterialSaveDto.builder()
                .name("Test steel")
                .description("First")
                .unit(Unit.KG)
                .build();

        mockMvc.perform(post("/api/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateMaterial_shouldReturnNoContent() throws Exception {
        MaterialData material = new MaterialData();
        material.setName("Old name");
        material.setDescription("Old description");
        material.setUnit(Unit.KG);

        MaterialData saved = materialRepository.save(material);

        MaterialSaveDto request = MaterialSaveDto.builder()
                .name("New name")
                .description("New description")
                .unit(Unit.PCS)
                .build();

        mockMvc.perform(put("/api/materials/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateMaterial_shouldChangeData() throws Exception {
        MaterialData material = new MaterialData();
        material.setName("Old name");
        material.setDescription("Old description");
        material.setUnit(Unit.KG);

        MaterialData saved = materialRepository.save(material);

        MaterialSaveDto request = MaterialSaveDto.builder()
                .name("New name")
                .description("New description")
                .unit(Unit.PCS)
                .build();

        mockMvc.perform(put("/api/materials/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        MaterialData updated = materialRepository.findById(saved.getId())
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals("New name", updated.getName());
        org.junit.jupiter.api.Assertions.assertEquals(Unit.PCS, updated.getUnit());
    }

    @Test
    void deleteMaterial_shouldReturnNoContent() throws Exception {
        MaterialData material = new MaterialData();
        material.setName("Test steel");
        material.setDescription("Test material");
        material.setUnit(Unit.KG);

        MaterialData saved = materialRepository.save(material);

        mockMvc.perform(delete("/api/materials/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertFalse(
                materialRepository.existsById(saved.getId())
        );
    }

    @Test
    void deleteMaterial_whenNotFound_shouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/materials/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateMaterial_withDuplicateName_shouldReturnConflict() throws Exception {
        MaterialData material1 = testDataFactory.createMaterial("Steel");
        MaterialData material2 = testDataFactory.createMaterial("Steel2");

        MaterialSaveDto request = MaterialSaveDto.builder()
                .name("Steel")
                .description("Updated description")
                .unit(Unit.KG)
                .build();

        mockMvc.perform(put("/api/materials/{id}", material2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteMaterial_whenReferencedByPurchaseRecord_shouldReturnConflict() throws Exception {
        MaterialData material = testDataFactory.createMaterial("Steel");
        testDataFactory.createPurchaseRecord(100L, material, 10.0);

        mockMvc.perform(delete("/api/materials/{id}", material.getId()))
                .andExpect(status().isConflict());
    }
}