package cl.duoc.bancoxyz.util;

import cl.duoc.bancoxyz.exception.FechaInvalidaException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class DateParser {

    private static final List<DateTimeFormatter> FORMATOS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    public static LocalDate parsear(String fechaCsv){
        for(DateTimeFormatter formato : FORMATOS){
            try{
                return LocalDate.parse(fechaCsv, formato);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new FechaInvalidaException("No se pudo parsear la fecha: " + fechaCsv);
    }
}
