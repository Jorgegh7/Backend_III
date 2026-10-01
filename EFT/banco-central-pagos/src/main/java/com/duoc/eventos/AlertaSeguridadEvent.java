package com.duoc.eventos;

import java.time.LocalDateTime;

/**
 * Evento publicado por banco-central-clientes cuando un usuario acumula
 * varios intentos de login fallidos consecutivos.
 */
public record AlertaSeguridadEvent(
        String username,
        String canal,
        int intentosFallidos,
        String motivo,
        LocalDateTime fechaAlerta
) {}