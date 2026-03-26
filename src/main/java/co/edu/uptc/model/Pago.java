package co.edu.uptc.model;

import java.time.LocalDate;

public class Pago {
    private static int contador = 1000;

    private int id;
    private double montoPagado, capital, interes, saldoInicial, saldoFinal;
    private LocalDate fechaPago;

    public Pago(int id, double montoPagado, double capital, double interes, double saldoInicial, double saldoFinal,
            LocalDate fechaPago) {
        this.id = contador++;
        this.montoPagado = montoPagado;
        this.capital = capital;
        this.interes = interes;
        this.saldoInicial = saldoInicial;
        this.saldoFinal = saldoFinal;
        this.fechaPago = fechaPago;
    }

    public Pago() {
    }

    public static int getContador() {
        return contador;
    }

    public static void setContador(int contador) {
        Pago.contador = contador;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(double montoPagado) {
        this.montoPagado = montoPagado;
    }

    public double getCapital() {
        return capital;
    }

    public void setCapital(double capital) {
        this.capital = capital;
    }

    public double getInteres() {
        return interes;
    }

    public void setInteres(double interes) {
        this.interes = interes;
    }

    public double getSaldoInicial() {
        return saldoInicial;
    }

    public void setSaldoInicial(double saldoInicial) {
        this.saldoInicial = saldoInicial;
    }

    public double getSaldoFinal() {
        return saldoFinal;
    }

    public void setSaldoFinal(double saldoFinal) {
        this.saldoFinal = saldoFinal;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    @Override
    public String toString() {
        return "Pago [id=" + id + ", montoPagado=" + montoPagado + ", capital=" + capital + ", interes=" + interes
                + ", saldoInicial=" + saldoInicial + ", saldoFinal=" + saldoFinal + ", fechaPago=" + fechaPago + "]";
    }

    
    
}
