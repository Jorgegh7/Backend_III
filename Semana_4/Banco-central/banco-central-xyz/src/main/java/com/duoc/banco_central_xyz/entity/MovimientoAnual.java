package com.duoc.banco_central_xyz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@Table(name = "movimientos_anuales")
@Entity
public class MovimientoAnual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cuenta_id_legacy")
    private Long cuentaIdLegacy;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private String transaccion;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(length = 255)
    private String descripcion;
}
