package co.edu.uptc.model;

import java.time.LocalDate;

public class ReporteIngreso {
    private double totalInteresesRecaudados;
    private int totalPrestamosActivos;
    private LocalDate generadoEn;

    public double getTotalInteresesRecaudados() {
        return totalInteresesRecaudados;
    }
    public int getTotalPrestamosActivos() {
        return totalPrestamosActivos;
    }
}
