package co.edu.uptc.controller;

import co.edu.uptc.model.RankingSocio;
import co.edu.uptc.model.ReporteIngreso;
import co.edu.uptc.service.ReporteServicio;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

public class ReporteControlador {

    private static final Logger logger = Logger.getLogger(ReporteControlador.class.getName());

    private final ReporteServicio reporteServicio;


    public ReporteControlador(ReporteServicio reporteServicio) {
        this.reporteServicio = reporteServicio;
    }

    public ReporteIngreso generarReporteIngresosPorIntereses(LocalDate inicio, LocalDate fin) {
        try {
            logger.info("Controlador: generando reporte de ingresos por intereses");
            ReporteIngreso reporte = reporteServicio.reporteIngresosPorIntereses(inicio, fin);
            logger.info("Controlador: reporte de ingresos generado exitosamente");
            return reporte;
        } catch (Exception e) {
            logger.warning("Controlador: error al generar reporte de ingresos - " + e.getMessage());
            throw e;
        }
    }

    public List<RankingSocio> rankingBeneficioSocios() {
        try {
            logger.info("Controlador: generando ranking de socios");
            List<RankingSocio> ranking = reporteServicio.rankingBeneficioSocios();
            logger.info("Controlador: ranking generado exitosamente");
            return ranking;
        } catch (Exception e) {
            logger.warning("Controlador: error al generar ranking - " + e.getMessage());
            throw e;
        }
    }

    public void exportarJson(Object datos, String ruta) {
        try {
            logger.info("Controlador: exportando reporte a JSON en: " + ruta);
            reporteServicio.exportarJson(datos, ruta);
            logger.info("Controlador: reporte JSON exportado exitosamente");
        } catch (Exception e) {
            logger.warning("Controlador: error al exportar JSON - " + e.getMessage());
            throw e;
        }
    }

    public void exportarCsv(List<?> datos, String ruta) {
        try {
            logger.info("Controlador: exportando reporte a CSV en: " + ruta);
            reporteServicio.exportarCsv(datos, ruta);
            logger.info("Controlador: reporte CSV exportado exitosamente");
        } catch (Exception e) {
            logger.warning("Controlador: error al exportar CSV - " + e.getMessage());
            throw e;
        }
    }
}