package co.edu.uptc.model;

import java.time.LocalDate;

public class CuotaAmortizacion {
    private int nimeroCuota;
    private double cuotaTotal, capital, interes, saldoInicial, salgoFinal;
    private boolean pagada;
    private LocalDate fechaVencimiento;

    public CuotaAmortizacion(int nimeroCuota, double cuotaTotal, double capital, double interes, double saldoInicial,
            double salgoFinal, boolean pagada, LocalDate fechaVencimiento) {
        this.nimeroCuota = nimeroCuota;
        this.cuotaTotal = cuotaTotal;
        this.capital = capital;
        this.interes = interes;
        this.saldoInicial = saldoInicial;
        this.salgoFinal = salgoFinal;
        this.pagada = pagada;
        this.fechaVencimiento = fechaVencimiento;
    }

    public CuotaAmortizacion() {
    }

    public int getNimeroCuota() {
        return nimeroCuota;
    }

    public void setNimeroCuota(int nimeroCuota) {
        this.nimeroCuota = nimeroCuota;
    }

    public double getCuotaTotal() {
        return cuotaTotal;
    }

    public void setCuotaTotal(double cuotaTotal) {
        this.cuotaTotal = cuotaTotal;
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

    public double getSalgoFinal() {
        return salgoFinal;
    }

    public void setSalgoFinal(double salgoFinal) {
        this.salgoFinal = salgoFinal;
    }

    public boolean isPagada() {
        return pagada;
    }

    public void setPagada(boolean pagada) {
        this.pagada = pagada;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    @Override
    public String toString() {
        return "CuotaAmortizacion [nimeroCuota=" + nimeroCuota + ", cuotaTotal=" + cuotaTotal + ", capital=" + capital
                + ", interes=" + interes + ", saldoInicial=" + saldoInicial + ", salgoFinal=" + salgoFinal + ", pagada="
                + pagada + ", fechaVencimiento=" + fechaVencimiento + "]";
    }

    
}
