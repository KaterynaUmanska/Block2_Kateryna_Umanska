package org.example.block2.utils;
import org.example.block2.data.MaterialData;
import org.example.block2.data.PurchaseRecordData;
import org.example.block2.dict.Unit;
import org.example.block2.dto.MaterialDto;
import org.example.block2.dto.MaterialSaveDto;
import org.example.block2.dto.PurchaseRecordDto;
import org.example.block2.dto.PurchaseRecordSaveDto;
import org.example.block2.repository.MaterialRepository;
import org.example.block2.repository.PurchaseRecordRepository;
import org.example.block2.service.MaterialService;
import org.example.block2.service.PurchaseRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TestDataFactory {

    private final MaterialRepository materialRepository;
    private final PurchaseRecordRepository purchaseRecordRepository;

    @Autowired(required = false)
    private MaterialService materialService;

    @Autowired(required = false)
    private PurchaseRecordService purchaseRecordService;

    public TestDataFactory(MaterialRepository materialRepository, PurchaseRecordRepository purchaseRecordRepository) {
        this.materialRepository = materialRepository;
        this.purchaseRecordRepository = purchaseRecordRepository;
    }

    public MaterialData createMaterial() {
        return createMaterial("Test steel");
    }

    public MaterialData createMaterial(String name) {
        MaterialData material = new MaterialData();
        material.setName(name);
        material.setDescription("Test material");
        material.setUnit(Unit.KG);
        return materialRepository.save(material);
    }

    public PurchaseRecordData createPurchaseRecord(Long orderId, MaterialData material, Double quantity) {
        PurchaseRecordData record = new PurchaseRecordData();
        record.setOrderId(orderId);
        record.setMaterial(material);
        record.setQuantity(BigDecimal.valueOf(quantity));
        return purchaseRecordRepository.save(record);
    }

    public MaterialDto createMaterialDto(String name, Unit unit) {
        return materialService.saveMaterial(
                MaterialSaveDto.builder()
                        .name(name)
                        .description("Test material")
                        .unit(unit)
                        .build()
        );
    }

    public PurchaseRecordDto createPurchaseRecordDto(Long orderId, Long materialId, Double quantity) {
        return purchaseRecordService.savePurchaseRecord(
                PurchaseRecordSaveDto.builder()
                        .orderId(orderId)
                        .materialId(materialId)
                        .quantity(BigDecimal.valueOf(quantity))
                        .build()
        );
    }
}
