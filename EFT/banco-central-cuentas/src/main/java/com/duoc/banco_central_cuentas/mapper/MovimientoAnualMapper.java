package com.duoc.banco_central_cuentas.mapper;

import com.duoc.banco_central_cuentas.dto.MovimientoAnualDto;
import com.duoc.banco_central_cuentas.entity.MovimientoAnual;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MovimientoAnualMapper {

    MovimientoAnualDto toDto(MovimientoAnual entidad);
}