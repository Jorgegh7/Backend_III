package com.duoc.banco_central_xyz.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class OAuth2TestController {

    @GetMapping("/api/oauth2/cuentas")
    public Map<String, Object> cuentasOAuth2(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "mensaje", "Acceso validado con OAuth2 Resource Server",
                "clientId", jwt.getClaimAsString("sub"),
                "scope", jwt.getClaimAsString("scope")
        );
    }
}