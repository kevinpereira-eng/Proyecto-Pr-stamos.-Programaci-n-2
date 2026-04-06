package co.edu.uptc.model;

import java.time.LocalDate;

public class ReporteIngreso {

    private double totalInteresesRecaudados;
    private int totalPrestamosActivos;
    private LocalDate generadoEn;

    public ReporteIngreso(double totalInteresesRecaudados, int totalPrestamosActivos) {
        this.totalInteresesRecaudados = totalInteresesRecaudados;
        this.totalPrestamosActivos = totalPrestamosActivos;
        this.generadoEn = LocalDate.now();
    }

    public double getTotalIntereses() {
        return totalInteresesRecaudados;
    }

    public int getTotalPrestamosActivos() {
        return totalPrestamosActivos;
    }

    public double getTotalInteresesRecaudados() {
        return totalInteresesRecaudados;
    }

    public void setTotalInteresesRecaudados(double totalInteresesRecaudados) {
        this.totalInteresesRecaudados = totalInteresesRecaudados;
    }

    public void setTotalPrestamosActivos(int totalPrestamosActivos) {
        this.totalPrestamosActivos = totalPrestamosActivos;
    }

    public LocalDate getGeneradoEn() {
        return generadoEn;
    }

    public void setGeneradoEn(LocalDate generadoEn) {
        this.generadoEn = generadoEn;
    }

    @Override
    public String toString() {
        return "ReporteIngreso{" +
                "totalInteresesRecaudados=" + totalInteresesRecaudados +
                ", totalPrestamosActivos=" + totalPrestamosActivos +
                ", generadoEn=" + generadoEn +
                '}';
    }
}