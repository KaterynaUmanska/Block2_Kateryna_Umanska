package org.example.block2.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.block2.data.MaterialData;
import org.example.block2.dto.MaterialSaveDto;
import org.example.block2.service.MaterialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {
    private final MaterialService materialService;

    @PostMapping
    public ResponseEntity<MaterialData> createMaterial(@Valid @RequestBody MaterialSaveDto dto) {
        log.info("REST request to create material: {}", dto.getName());
        MaterialData created = materialService.saveMaterial(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<MaterialData>> getAllMaterials() {
        log.info("REST request to get all materials");
        List<MaterialData> materials = materialService.getAllMaterials();
        return ResponseEntity.ok(materials);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialData> getMaterialById(@PathVariable Long id) {
        log.info("REST request to get material by ID: {}", id);
        MaterialData material = materialService.getMaterialById(id);
        return ResponseEntity.ok(material);
    }
}
