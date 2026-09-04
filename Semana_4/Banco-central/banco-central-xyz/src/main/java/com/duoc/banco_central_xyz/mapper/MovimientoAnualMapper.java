package com.duoc.banco_central_xyz.mapper;

import com.duoc.banco_central_xyz.dto.MovimientoAnualDto;
import com.duoc.banco_central_xyz.entity.MovimientoAnual;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MovimientoAnualMapper {

    MovimientoAnualDto toDto(MovimientoAnual entidad);
}