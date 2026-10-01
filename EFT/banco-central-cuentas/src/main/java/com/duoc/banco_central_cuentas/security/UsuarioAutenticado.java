package com.duoc.banco_central_cuentas.security;

import java.security.Principal;

public record UsuarioAutenticado(String username, String rol, Long cuentaIdLegacy) implements Principal {

    @Override
    public String getName() {
        return username;
    }

    public boolean esEmpleado() {
        return "EMPLEADO".equals(rol);
    }
}