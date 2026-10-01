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

    //Tasas mensuales (se construyen desde String para evitar la imprecision de new BigDecimal(double))
    private static final BigDecimal TASA_AHORRO = new BigDecimal("0.005");
    private static final BigDecimal TASA_PRESTAMO = new BigDecimal("0.015");
    private static final BigDecimal TASA_HIPOTECA = new BigDecimal("0.008");

    //Cantidad de meses que cubre el calculo: interes trimestral proporcional (tasa mensual x 3, interes simple)
    private static final BigDecimal MESES_PERIODO = new BigDecimal("3");

    private static final int EDAD_MINIMA = 18;
    private static final int EDAD_MAXIMA = 120;

    @Override
    public CuentaBancaria process(CuentaBancariaDto dto) throws Exception {

        //Verifica si el tipo pertenece a la lista TIPOS_VALIDOS
        String tipo = dto.getTipo();
        if (!TIPOS_VALIDOS.contains(tipo)) {
            throw new RegistroInvalidoException(
                    "Tipo de cuenta inválido para cuenta_id=" + dto.getCuentaId() + ": " + tipo);
        }

        //Verifica que nombre no sea Null, este vacio o sea Unknown
        if (dto.getNombre() == null || dto.getNombre().isBlank() || dto.getNombre().equalsIgnoreCase("unknown")) {
            throw new RegistroInvalidoException(
                    "Nombre inválido para cuenta_id=" + dto.getCuentaId() + ": " + dto.getNombre());
        }

        //Verifica que el saldo no esta vacio
        if (dto.getSaldo() == null || dto.getSaldo().isBlank()) {
            throw new RegistroInvalidoException("Saldo vacío para cuenta_id=" + dto.getCuentaId());
        }

        //Verifica que la edad no esta vacia
        if (dto.getEdad() == null || dto.getEdad().isBlank()) {
            throw new RegistroInvalidoException("Edad vacía para cuenta_id=" + dto.getCuentaId());
        }

        //Verifica un rango de edad valido
        int edad = Integer.parseInt(dto.getEdad());
        if (edad < EDAD_MINIMA || edad > EDAD_MAXIMA) {
            throw new RegistroInvalidoException(
                    "Edad fuera de rango (" + EDAD_MINIMA + "-" + EDAD_MAXIMA + ") para cuenta_id=" +
                            dto.getCuentaId() + ": " + edad);
        }

        //Verifica que el saldo sea mayor a 0
        BigDecimal saldo = new BigDecimal(dto.getSaldo());
        if (saldo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegistroInvalidoException(
                    "Saldo no positivo para cuenta_id=" + dto.getCuentaId() + ": " + saldo);
        }

        //Aplica tasa, obtiene el interes generado y el saldo final
        BigDecimal tasa = obtenerTasa(tipo);
        BigDecimal interes = saldo.multiply(tasa).multiply(MESES_PERIODO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal saldoFinal = saldo.add(interes);

        return CuentaBancaria.builder()
                .cuentaIdLegacy(Long.parseLong(dto.getCuentaId()))
                .nombre(dto.getNombre())
                .saldo(saldo)
                .edad(edad)
                .tipo(tipo)
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