package com.duoc.bff_mobile.controller;

import com.duoc.bff_mobile.dto.SaldoResponseDto;
import com.duoc.bff_mobile.service.MobileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bff-mobile/cuentas")
@RequiredArgsConstructor
public class MobileController {

    private final MobileService mobileService;

    @GetMapping("/{id}/saldo")
    public SaldoResponseDto obtenerSaldo(@PathVariable Long id,
                                         @RequestHeader("Authorization") String token) {
        return mobileService.consultarSaldo(id, token);
    }

}