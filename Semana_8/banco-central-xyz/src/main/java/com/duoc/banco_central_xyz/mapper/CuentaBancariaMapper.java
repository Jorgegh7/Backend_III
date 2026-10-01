package com.duoc.banco_central_xyz.mapper;

import com.duoc.banco_central_xyz.dto.CuentaBancariaDto;
import com.duoc.banco_central_xyz.entity.CuentaBancaria;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CuentaBancariaMapper {

    CuentaBancariaDto toDto(CuentaBancaria entidad);
}