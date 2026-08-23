package cl.duoc.bancoxyz.processor;

import cl.duoc.bancoxyz.dtos.TransaccionDto;
import cl.duoc.bancoxyz.entities.Transaccion;
import cl.duoc.bancoxyz.exception.RegistroInvalidoException;
import cl.duoc.bancoxyz.util.DateParser;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public class TransaccionProcessor implements ItemProcessor<TransaccionDto, Transaccion> {

    private static final Set<String> TIPOS_VALIDOS = Set.of("debito", "credito");

    @Override
    public Transaccion process(TransaccionDto dto) throws Exception {

        //Validamos que monto no este vacio
        if(dto.getMonto() == null || dto.getMonto().isBlank()){
            throw new RegistroInvalidoException( "Monto vacío para id_legacy=" + dto.getId());
        }

        //Validamos que tipo corresponda a TIPOS_VALIDOS
        String tipo = dto.getTipo();
        if(!TIPOS_VALIDOS.contains(tipo)){
            throw new RegistroInvalidoException("Monto vacío para id_legacy=" + dto.getId());
        }

        //Parseamos la fecha
        LocalDate fecha = DateParser.parsear(dto.getFecha());

        //Validamos que el monto no sea negativo
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
