package cl.duoc.bancoxyz.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoAnualDto {

    private String cuentaId;
    private String fecha;
    private String transaccion;
    private String monto;
    private String descripcion;

}
