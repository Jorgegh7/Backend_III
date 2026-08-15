package cl.duoc.bancoxyz.util;

import cl.duoc.bancoxyz.exception.FechaInvalidaException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateParser {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static LocalDate parsear(String fechaCsv){
        try{
            return LocalDate.parse(fechaCsv, FORMATO);
        } catch (DateTimeParseException e) {
            throw new FechaInvalidaException("No se pudo parsear la fecha: " + fechaCsv);
        }
    }

}
