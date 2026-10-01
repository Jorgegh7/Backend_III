package com.duoc.banco_central_cuentas.mapper;

import com.duoc.banco_central_cuentas.dto.CuentaBancariaDto;
import com.duoc.banco_central_cuentas.entity.CuentaBancaria;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CuentaBancariaMapper {

    CuentaBancariaDto toDto(CuentaBancaria entidad);
}