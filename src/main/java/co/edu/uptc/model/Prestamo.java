package co.edu.uptc.model;

import java.time.LocalDate;
import java.util.ArrayList;

public class Prestamo {
    private static int contador = 1;

    private int codigo, plazoMeses;
    private double montoSolicitado, tasaInteresAnual;
    private EstadoPrestamo estado;
    private LocalDate fechaInicio;
    private ArrayList<Pago> pagos;
    
    public Prestamo(int codigo, int plazoMeses, double montoSolicitado, double tasaInteresAnual, EstadoPrestamo estado,
            LocalDate fechaInicio) {
        this.codigo = contador++;
        this.plazoMeses = plazoMeses;
        this.montoSolicitado = montoSolicitado;
        this.tasaInteresAnual = tasaInteresAnual;
        this.estado = estado;
        this.fechaInicio = fechaInicio;
    }

    public Prestamo(ArrayList<Pago> pagos) {
        this.pagos = pagos;
    }

    public static int getContador() {
        return contador;
    }

    public static void setContador(int contador) {
        Prestamo.contador = contador;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public int getPlazoMeses() {
        return plazoMeses;
    }

    public void setPlazoMeses(int plazoMeses) {
        this.plazoMeses = plazoMeses;
    }

    public double getMontoSolicitado() {
        return montoSolicitado;
    }

    public void setMontoSolicitado(double montoSolicitado) {
        this.montoSolicitado = montoSolicitado;
    }

    public double getTasaInteresAnual() {
        return tasaInteresAnual;
    }

    public void setTasaInteresAnual(double tasaInteresAnual) {
        this.tasaInteresAnual = tasaInteresAnual;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public ArrayList<Pago> getPagos() {
        return pagos;
    }

    public void setPagos(ArrayList<Pago> pagos) {
        this.pagos = pagos;
    }

    @Override
    public String toString() {
        return "Prestamo [codigo=" + codigo + ", plazoMeses=" + plazoMeses + ", montoSolicitado=" + montoSolicitado
                + ", tasaInteresAnual=" + tasaInteresAnual + ", estado=" + estado + ", fechaInicio=" + fechaInicio
                + ", pagos=" + pagos + "]";
    }

    
}
