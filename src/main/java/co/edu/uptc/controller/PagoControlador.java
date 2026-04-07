package co.edu.uptc.controller;

import co.edu.uptc.model.CuotaAmortizacion;
import co.edu.uptc.model.Pago;
import co.edu.uptc.service.PagoServicio;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

public class PagoControlador {

    private static final Logger logger = Logger.getLogger(PagoControlador.class.getName());

    private final PagoServicio pagoServicio;

    public PagoControlador(PagoServicio pagoServicio) {
        this.pagoServicio = pagoServicio;
    }

    /**
     * Registra el pago de la siguiente cuota del préstamo (cuota según tabla de amortización).
     */
    public Pago registrarPago(int idPrestamo, LocalDate fechaPago) {
        try {
            logger.info("Controlador: registrando pago para préstamo " + idPrestamo);
            Pago pago = pagoServicio.registrarPago(idPrestamo, fechaPago);
            logger.info("Controlador: pago registrado exitosamente - ID " + pago.getId());
            return pago;
        } catch (RuntimeException e) {
            logger.warning("Controlador: error al registrar pago - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Registra un pago anticipado o abono extra con monto personalizado.
     */
    public Pago registrarPagoAnticipado(int idPrestamo, double monto, LocalDate fechaPago) {
        try {
            logger.info("Controlador: registrando pago anticipado para préstamo " + idPrestamo);
            Pago pago = pagoServicio.registrarPagoAnticipado(idPrestamo, monto, fechaPago);
            logger.info("Controlador: pago anticipado registrado - ID " + pago.getId());
            return pago;
        } catch (RuntimeException e) {
            logger.warning("Controlador: error al registrar pago anticipado - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Lista el historial de pagos de un préstamo.
     */
    public List<Pago> listarPagos(int idPrestamo) {
        try {
            logger.info("Controlador: listando pagos del préstamo " + idPrestamo);
            return pagoServicio.listarPagosPorPrestamo(idPrestamo);
        } catch (Exception e) {
            logger.warning("Controlador: error al listar pagos - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Obtiene la información de la próxima cuota a pagar.
     */
    public CuotaAmortizacion obtenerProximaCuota(int idPrestamo) {
        try {
            logger.info("Controlador: obteniendo próxima cuota del préstamo " + idPrestamo);
            return pagoServicio.obtenerProximaCuota(idPrestamo);
        } catch (Exception e) {
            logger.warning("Controlador: error al obtener próxima cuota - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retorna el total de intereses pagados hasta el momento.
     */
    public double obtenerTotalInteresesPagados(int idPrestamo) {
        try {
            logger.info("Controlador: calculando total de intereses pagados del préstamo " + idPrestamo);
            return pagoServicio.calcularTotalInteresesPagados(idPrestamo);
        } catch (Exception e) {
            logger.warning("Controlador: error al calcular intereses - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retorna el total de capital abonado hasta el momento.
     */
    public double obtenerTotalCapitalPagado(int idPrestamo) {
        try {
            logger.info("Controlador: calculando total de capital pagado del préstamo " + idPrestamo);
            return pagoServicio.calcularTotalCapitalPagado(idPrestamo);
        } catch (Exception e) {
            logger.warning("Controlador: error al calcular capital pagado - " + e.getMessage());
            throw e;
        }
    }
}
