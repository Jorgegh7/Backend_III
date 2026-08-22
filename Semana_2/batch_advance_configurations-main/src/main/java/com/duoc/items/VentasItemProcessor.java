package com.duoc.items;

import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;

import com.duoc.business.InformeVenta;
import com.duoc.business.InvalidDataException;
import com.duoc.business.Venta;

@Component
public class VentasItemProcessor implements ItemProcessor<Venta, InformeVenta> {
    private static final Logger logger = LoggerFactory.getLogger(VentasItemProcessor.class);

    @Override
    public InformeVenta process(Venta venta) throws Exception {
        if (venta.getCantidad() <= 0) {
            logger.warn("Cantidad inválida para producto {}: {}", venta.getProducto(), venta.getCantidad());
            throw new InvalidDataException("Cantidad debe ser mayor que 0");
        }

        if (venta.getPrecio() == null || venta.getPrecio() <= 0) {
            logger.warn("Precio inválido para producto {}: {}", venta.getProducto(), venta.getPrecio());
            throw new InvalidDataException("Precio debe ser mayor que 0");
        }

        InformeVenta informe = new InformeVenta();
        informe.setProducto(venta.getProducto());
        informe.setCantidadTotal(venta.getCantidad());
        informe.setTotalVentas(venta.getCantidad() * venta.getPrecio());
        return informe;
    }
}