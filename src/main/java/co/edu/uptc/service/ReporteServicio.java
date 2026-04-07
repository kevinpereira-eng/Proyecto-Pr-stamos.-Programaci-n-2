package co.edu.uptc.service;

import co.edu.uptc.model.Prestamo;
import co.edu.uptc.model.RankingSocio;
import co.edu.uptc.model.ReporteIngreso;
import co.edu.uptc.model.Socio;
import co.edu.uptc.repository.IPrestamoRepositorio;
import co.edu.uptc.repository.ISocioRepositorio;
import co.edu.uptc.util.ExportadorReporte;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ReporteServicio {

    private static final Logger logger = Logger.getLogger(ReporteServicio.class.getName());

    private final IPrestamoRepositorio prestamoRepo; // co.edu.uptc.repository.IPrestamoRepositorio
    private final ISocioRepositorio socioRepo;
    private final ExportadorReporte exportador;


    public ReporteServicio(IPrestamoRepositorio prestamoRepo, ISocioRepositorio socioRepo, ExportadorReporte exportador) {
        this.prestamoRepo = prestamoRepo;
        this.socioRepo = socioRepo;
        this.exportador = exportador;
    }


    public ReporteIngreso reporteIngresosPorIntereses(LocalDate inicio, LocalDate fin) {
        logger.info("Generando reporte de ingresos por intereses entre: " + inicio + " y " + fin);

        List<Prestamo> prestamos = prestamoRepo.listarTodos();

        // Sumar intereses de préstamos dentro del periodo
        double totalIntereses = prestamos.stream()
                .filter(p -> !p.getFechaInicio().isBefore(inicio) && !p.getFechaInicio().isAfter(fin))
                .mapToDouble(p -> p.calcularInteresAcumulado())
                .sum();

        // Contar préstamos activos
        int totalActivos = (int) prestamos.stream()
                .filter(p -> p.getEstado().name().equals("ACTIVO"))
                .count();

        logger.info("Reporte generado - Intereses: " + totalIntereses + ", Activos: " + totalActivos);

        return new ReporteIngreso(totalIntereses, totalActivos);
    }

    public List<RankingSocio> rankingBeneficioSocios() {
        logger.info("Generando ranking de socios por monto financiado");

        List<Socio> socios = socioRepo.listarTodos();

        // Construir ranking, ordenar de mayor a menor y tomar top 5
        List<RankingSocio> ranking = socios.stream()
                .map(s -> {
                    double montoTotal = s.getPrestamos() == null ? 0 :
                            s.getPrestamos().stream()
                                    .mapToDouble(Prestamo::getMontoSolicitado)
                                    .sum();

                    int cantidadPrestamos = s.getPrestamos() == null ? 0 : s.getPrestamos().size();

                    return new RankingSocio(
                            s.getId(),
                            s.getNombre(),
                            montoTotal,
                            cantidadPrestamos
                    );
                })
                .sorted(Comparator.comparingDouble(RankingSocio::getMontoTotalFinanciado).reversed())
                .limit(5) // top 5 según el PDF
                .collect(Collectors.toList());

        logger.info("Ranking generado con " + ranking.size() + " socios");
        return ranking;
    }

    public void exportarJson(Object datos, String ruta) {
        logger.info("Exportando reporte a JSON en: " + ruta);
        exportador.exportarJson(datos, ruta);
        logger.info("Reporte JSON exportado exitosamente en: " + ruta);
    }

    public void exportarCsv(List<?> datos, String ruta) {
        logger.info("Exportando reporte a CSV en: " + ruta);
        exportador.exportarCsv(datos, ruta);
        logger.info("Reporte CSV exportado exitosamente en: " + ruta);
    }
}