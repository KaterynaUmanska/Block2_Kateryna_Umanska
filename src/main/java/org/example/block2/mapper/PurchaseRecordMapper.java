package org.example.block2.mapper;

import org.example.block2.data.MaterialData;
import org.example.block2.data.PurchaseRecordData;
import org.example.block2.dto.PurchaseRecordDto;
import org.example.block2.dto.PurchaseRecordListDto;
import org.example.block2.dto.PurchaseRecordSaveDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {MaterialMapper.class})
public interface PurchaseRecordMapper {

    PurchaseRecordDto toDto(PurchaseRecordData data);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "material", source = "material")
    PurchaseRecordData toEntity(PurchaseRecordSaveDto dto, MaterialData material);

    @Mapping(target = "materialName", source = "material.name")
    PurchaseRecordListDto toListDto(PurchaseRecordData data);
}
