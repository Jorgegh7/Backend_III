package cl.duoc.bancoxyz.processor;

import cl.duoc.bancoxyz.dtos.TransaccionDto;
import cl.duoc.bancoxyz.entities.Transaccion;
import cl.duoc.bancoxyz.exception.RegistroInvalidoException;
import cl.duoc.bancoxyz.util.DateParser;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransaccionProcessor implements ItemProcessor<TransaccionDto, Transaccion> {
    @Override
    public Transaccion process(TransaccionDto dto) throws Exception {

        LocalDate fecha = DateParser.parsear(dto.getFecha());

        BigDecimal monto = new BigDecimal(dto.getMonto());
        if(monto.compareTo(BigDecimal.ZERO) <= 0){
            throw new RegistroInvalidoException(
                    "Monto invalido(negativo o cero) para id_legacy = " + dto.getId() + ":" + monto
            );
        }
        return Transaccion.builder()
                .idLegacy(Long.parseLong(dto.getId()))
                .fecha(fecha)
                .monto(monto)
                .tipo(dto.getTipo())
                .build();
    }

}
