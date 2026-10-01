package com.duoc.banco_central_clientes.controller;

import com.duoc.banco_central_clientes.dto.ClienteDto;
import com.duoc.banco_central_clientes.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuth2ClienteController {

    private final UsuarioRepository usuarioRepository;

    public OAuth2ClienteController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Consulta servicio a servicio: devuelve el rol y la cuenta de un usuario. Exige scope clientes.read.
    @GetMapping("/api/oauth2/clientes/{username}")
    public ResponseEntity<ClienteDto> obtenerCliente(@PathVariable String username) {
        return usuarioRepository.findByUsername(username)
                .map(u -> ResponseEntity.ok(new ClienteDto(username, u.getRol().name(), u.getCuentaIdLegacy())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}