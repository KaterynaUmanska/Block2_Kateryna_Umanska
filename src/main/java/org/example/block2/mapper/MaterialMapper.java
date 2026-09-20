package org.example.block2.mapper;

import org.example.block2.data.MaterialData;
import org.example.block2.dto.MaterialDto;
import org.example.block2.dto.MaterialSaveDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MaterialMapper {

    MaterialDto toDto(MaterialData data);

    @Mapping(target = "id", ignore = true)
    MaterialData toEntity(MaterialSaveDto dto);
}
