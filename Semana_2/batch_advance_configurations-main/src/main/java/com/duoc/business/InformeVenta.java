package com.duoc.business;

public class InformeVenta {
    private String producto;
    private int cantidadTotal;
    private double totalVentas;

    public InformeVenta() {}
    
    public String getProducto() {
        return producto;
    }

    public int getCantidadTotal() {
        return cantidadTotal;
    }

    public double getTotalVentas() {
        return totalVentas;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public void setCantidadTotal(int cantidadTotal) {
        this.cantidadTotal = cantidadTotal;
    }
    
    public void setTotalVentas(double totalVentas) {
        this.totalVentas = totalVentas;
    }

    
}
