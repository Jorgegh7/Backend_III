package com.duoc.banco_central_pagos.mapper;

import com.duoc.banco_central_pagos.dto.TransaccionDto;
import com.duoc.banco_central_pagos.entity.Transaccion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransaccionMapper {

    TransaccionDto toDto(Transaccion entidad);
}
