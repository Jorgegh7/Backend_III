package cl.duoc.bancoxyz.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaccion {
    private Long idLegacy;
    private LocalDate fecha;
    private BigDecimal monto;
    private String tipo;
}