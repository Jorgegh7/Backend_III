package cl.duoc.bancoxyz.processor;

import cl.duoc.bancoxyz.dtos.MovimientoAnualDto;
import cl.duoc.bancoxyz.entities.MovimientoAnual;
import cl.duoc.bancoxyz.exception.RegistroInvalidoException;
import cl.duoc.bancoxyz.util.DateParser;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public class MovimientoAnualProcessor implements ItemProcessor<MovimientoAnualDto, MovimientoAnual> {

    private static final Set<String> TIPOS_VALIDOS = Set.of("deposito", "retiro", "compra");
    private static final Set<String> TIPOS_SALIDA = Set.of("retiro", "compra");
    private static final int DESCRIPTION_MAX_LENGTH = 255;

    @Override
    public MovimientoAnual process(MovimientoAnualDto dto) throws Exception {

        String transaccion = dto.getTransaccion();

        if(!TIPOS_VALIDOS.contains(transaccion)){
            throw new RegistroInvalidoException("Tipo de transacción inválido para cuenta_id=" + dto.getCuentaId() +
                    ": " + transaccion);
        }

        LocalDate fecha = DateParser.parsear(dto.getFecha());
        BigDecimal monto = new BigDecimal(dto.getMonto());

        boolean esSalida = TIPOS_SALIDA.contains(transaccion);

        if(esSalida && monto.compareTo(BigDecimal.ZERO)  >= 0){
            throw new RegistroInvalidoException("Monto inválido para " + transaccion +
                    " (debería ser negativo) en cuenta_id=" + dto.getCuentaId() + ": " + monto);
        }

        if(!esSalida && monto.compareTo(BigDecimal.ZERO) <= 0){
            throw new RegistroInvalidoException("Monto inválido para " + transaccion + " (debería ser positivo) en cuenta_id="
                    + dto.getCuentaId() + ": " + monto);
        }

        String descripcion = dto.getDescripcion();
        if(descripcion != null && descripcion.length() > DESCRIPTION_MAX_LENGTH){
            throw new RegistroInvalidoException("Descripción excede el largo máximo para cuenta_id=" + dto.getCuentaId()
                    + ": " + descripcion.length() + " caracteres");
        }

        return MovimientoAnual.builder()
                .cuentaIdLegacy(Long.parseLong(dto.getCuentaId()))
                .fecha(fecha)
                .transaccion(dto.getTransaccion())
                .monto(monto)
                .descripcion(descripcion)
                .build();
    }
}
