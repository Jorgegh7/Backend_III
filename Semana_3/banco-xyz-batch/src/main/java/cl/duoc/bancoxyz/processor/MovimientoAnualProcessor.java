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

    private static final Set<String> TIPOS_VALIDOS = Set.of("deposito", "retiro", "compra", "pago");
    private static final int DESCRIPTION_MAX_LENGTH = 255;

    @Override
    public MovimientoAnual process(MovimientoAnualDto dto) throws Exception {

        //Normaliza depósito a deposito
        String transaccionNormalizada = dto.getTransaccion().replace("ó", "o");

        //Verifica tipos
        if(!TIPOS_VALIDOS.contains(transaccionNormalizada)){
            throw new RegistroInvalidoException("Tipo de transacción inválido para cuenta_id=" + dto.getCuentaId() +
                    ": " + transaccionNormalizada);
        }

        LocalDate fecha = DateParser.parsear(dto.getFecha());

        //Verifica que el monto no sea Null y no este vacio
        if (dto.getMonto() == null || dto.getMonto().isBlank()) {
            throw new RegistroInvalidoException("Monto vacío para cuenta_id=" + dto.getCuentaId());
        }

        //Verifica que dto.getMonto() sea un valor numerico y no tenga caracteres
        BigDecimal monto;
        try{
            monto = new BigDecimal(dto.getMonto());
        }catch (Exception e){
            throw new RegistroInvalidoException(
                    "Monto inválido para " + transaccionNormalizada + " en cuenta_id=" + dto.getCuentaId() + ": " + dto.getMonto()
            );

        }

        //Verifica que monto no sea igual a 0
        if(monto.compareTo(BigDecimal.ZERO)  == 0){
            throw new RegistroInvalidoException("Monto inválido para " + transaccionNormalizada +
                    " (debería distinto a 0) en cuenta_id=" + dto.getCuentaId() + ": " + monto);
        }

        //Verifica un tamaño maximo para la descripcion
        String descripcion = dto.getDescripcion();
        if(descripcion != null && descripcion.length() > DESCRIPTION_MAX_LENGTH){
            throw new RegistroInvalidoException("Descripción excede el largo máximo para cuenta_id=" + dto.getCuentaId()
                    + ": " + descripcion.length() + " caracteres");
        }

        return MovimientoAnual.builder()
                .cuentaIdLegacy(Long.parseLong(dto.getCuentaId()))
                .fecha(fecha)
                .transaccion(transaccionNormalizada)
                .monto(monto)
                .descripcion(descripcion)
                .build();
    }
}
