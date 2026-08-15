package cl.duoc.bancoxyz.processor;

import cl.duoc.bancoxyz.dtos.CuentaBancariaDto;
import cl.duoc.bancoxyz.entities.CuentaBancaria;
import cl.duoc.bancoxyz.exception.RegistroInvalidoException;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

public class CuentaBancariaProcessor implements ItemProcessor<CuentaBancariaDto, CuentaBancaria> {

    private static final Set<String> TIPOS_VALIDOS = Set.of("ahorro", "prestamo", "hipoteca");

    private static final BigDecimal TASA_AHORRO = new BigDecimal(0.005);
    private static final BigDecimal TASA_PRESTAMO = new BigDecimal(0.015);
    private static final BigDecimal TASA_HIPOTECA = new BigDecimal(0.008);

    @Override
    public  CuentaBancaria process(CuentaBancariaDto dto) throws Exception {

        if (!TIPOS_VALIDOS.contains(dto.getTipo())) {
            throw new RegistroInvalidoException(
                    "Tipo de cuenta inválido para cuenta_id=" + dto.getCuentaId() + ": " + dto.getTipo());
        }

        int edad = Integer.parseInt(dto.getEdad());
        if(edad < 18 || edad > 90){
            throw  new RegistroInvalidoException(
                    "Registro invalido para cuenta id= " + dto.getCuentaId() + ":" + dto.getTipo());
        }

        BigDecimal saldo = new BigDecimal(dto.getSaldo());
        if(saldo.compareTo(BigDecimal.ZERO) <= 0){
            throw new RegistroInvalidoException("Saldo negativo para cuenta_id: " + dto.getCuentaId() + ": " + saldo);
        }

        BigDecimal tasa = obtenerTasa(dto.getTipo());
        BigDecimal interes = saldo.multiply(tasa).setScale(2, RoundingMode.HALF_UP);
        BigDecimal saldoFinal = saldo.add(interes);

        return CuentaBancaria.builder()
                .cuentaIdLegacy(Long.parseLong(dto.getCuentaId()))
                .nombre(dto.getNombre())
                .saldo(saldo)
                .edad(edad)
                .tipo(dto.getTipo())
                .interes(interes)
                .saldoFinal(saldoFinal)
                .build();
    }

    private BigDecimal obtenerTasa(String tipo) {
        return switch (tipo) {
            case "ahorro" -> TASA_AHORRO;
            case "prestamo" -> TASA_PRESTAMO;
            case "hipoteca" -> TASA_HIPOTECA;
            default -> throw new RegistroInvalidoException("Tipo sin tasa definida: " + tipo);
        };
    }
}
