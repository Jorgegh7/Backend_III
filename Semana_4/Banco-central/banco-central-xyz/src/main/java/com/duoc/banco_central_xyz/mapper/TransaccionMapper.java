package com.duoc.banco_central_xyz.mapper;

import com.duoc.banco_central_xyz.dto.TransaccionDto;
import com.duoc.banco_central_xyz.entity.Transaccion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransaccionMapper {

    TransaccionDto toDto(Transaccion entidad);
}
