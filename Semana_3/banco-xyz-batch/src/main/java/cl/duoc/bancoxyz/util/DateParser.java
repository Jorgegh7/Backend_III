package cl.duoc.bancoxyz.util;

import cl.duoc.bancoxyz.exception.FechaInvalidaException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Parsea fechas probando varios formatos en orden (yyyy-MM-dd,
 * yyyy/MM/dd, dd-MM-yyyy, dd/MM/yyyy)
 * Si ningun formato matchea, lanza FechaInvalidaException — la
 * politica de skip la trata por separado de los errores de datos
 * (RegistroInvalidoException), con su propio limite y contador.
 */
public class DateParser {

    private static final List<DateTimeFormatter> FORMATOS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
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
