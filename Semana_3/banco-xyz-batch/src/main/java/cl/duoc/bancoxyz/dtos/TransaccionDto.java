package cl.duoc.bancoxyz.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionDto {

    private String id;
    private String fecha;
    private String monto;
    private String tipo;
}
