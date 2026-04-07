package co.edu.uptc.model;

import java.time.LocalDate;
import java.util.ArrayList;

public class Prestamo {
    private static int contador = 1;

    private int codigo;
    private int plazoMeses;
    private double montoSolicitado;
    private double tasaInteresAnual;
    private EstadoPrestamo estado;
    private LocalDate fechaInicio;
    private ArrayList<Pago> pagos = new ArrayList<>();
    
    public Prestamo(int codigo, int plazoMeses, double montoSolicitado, double tasaInteresAnual, EstadoPrestamo estado,
            LocalDate fechaInicio) {
        this.codigo = contador++;
        this.plazoMeses = plazoMeses;
        this.montoSolicitado = montoSolicitado;
        this.tasaInteresAnual = tasaInteresAnual;
        this.estado = estado;
        this.fechaInicio = fechaInicio;
    }

    public Prestamo() {
        this.codigo = contador++;
    }

    public static void setContador(int nuevoValor) {
        contador = nuevoValor;
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
        this.pagos = (pagos != null) ? pagos : new ArrayList<>();
    }

    /**
     * Calcula el total de intereses acumulados (suma de intereses de todos los pagos realizados).
     */
    public double calcularInteresAcumulado() {
        if (pagos == null || pagos.isEmpty()) return 0;
        return pagos.stream().mapToDouble(Pago::getInteres).sum();
    }

    @Override
    public String toString() {
        return "Prestamo [codigo=" + codigo + ", plazoMeses=" + plazoMeses + ", montoSolicitado=" + montoSolicitado
                + ", tasaInteresAnual=" + tasaInteresAnual + ", estado=" + estado + ", fechaInicio=" + fechaInicio
                + ", pagos=" + pagos + "]";
    }
}
