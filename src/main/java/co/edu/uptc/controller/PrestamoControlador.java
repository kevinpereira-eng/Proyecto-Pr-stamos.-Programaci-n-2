package co.edu.uptc.controller;

import co.edu.uptc.model.CuotaAmortizacion;
import co.edu.uptc.model.Prestamo;
import co.edu.uptc.service.PrestamoServicio;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class PrestamoControlador {

    private static final Logger logger = Logger.getLogger(PrestamoControlador.class.getName());

    private final PrestamoServicio prestamoServicio;

    public PrestamoControlador(PrestamoServicio prestamoServicio) {
        this.prestamoServicio = prestamoServicio;
    }

    // ─── CRUD ────────────────────────────────────────────────────────────────

    public Prestamo registrar(int idSocio, double montoSolicitado,
                               double tasaInteresAnual, int plazoMeses,
                               LocalDate fechaInicio) {
        try {
            logger.info("Controlador: registrando préstamo para socio " + idSocio);
            Prestamo p = prestamoServicio.registrarPrestamo(
                    idSocio, montoSolicitado, tasaInteresAnual, plazoMeses, fechaInicio);
            logger.info("Controlador: préstamo registrado con código " + p.getCodigo());
            return p;
        } catch (RuntimeException e) {
            logger.warning("Controlador: error al registrar préstamo - " + e.getMessage());
            throw e;
        }
    }

    public Optional<Prestamo> consultar(int id) {
        try {
            logger.info("Controlador: consultando préstamo " + id);
            return prestamoServicio.buscarPorId(id);
        } catch (Exception e) {
            logger.warning("Controlador: error al consultar préstamo - " + e.getMessage());
            throw e;
        }
    }

    public List<Prestamo> listar() {
        try {
            logger.info("Controlador: listando todos los préstamos");
            return prestamoServicio.listarTodos();
        } catch (Exception e) {
            logger.warning("Controlador: error al listar préstamos - " + e.getMessage());
            throw e;
        }
    }

    public List<Prestamo> listarPorSocio(int idSocio) {
        try {
            logger.info("Controlador: listando préstamos del socio " + idSocio);
            return prestamoServicio.listarPorSocio(idSocio);
        } catch (Exception e) {
            logger.warning("Controlador: error al listar préstamos del socio - " + e.getMessage());
            throw e;
        }
    }

    public void eliminar(int id) {
        try {
            logger.info("Controlador: eliminando préstamo " + id);
            prestamoServicio.eliminarPrestamo(id);
            logger.info("Controlador: préstamo eliminado " + id);
        } catch (RuntimeException e) {
            logger.warning("Controlador: error al eliminar préstamo - " + e.getMessage());
            throw e;
        }
    }

    // ─── Tabla de amortización ────────────────────────────────────────────────

    public List<CuotaAmortizacion> obtenerTablaAmortizacion(int idPrestamo) {
        try {
            logger.info("Controlador: generando tabla de amortización para préstamo " + idPrestamo);
            return prestamoServicio.generarTablaAmortizacion(idPrestamo);
        } catch (Exception e) {
            logger.warning("Controlador: error al generar tabla - " + e.getMessage());
            throw e;
        }
    }

    public double obtenerCuotaMensual(int idPrestamo) {
        try {
            Prestamo p = prestamoServicio.buscarPorId(idPrestamo)
                    .orElseThrow(() -> new RuntimeException("Préstamo no encontrado: " + idPrestamo));
            return prestamoServicio.calcularCuotaMensual(p);
        } catch (Exception e) {
            logger.warning("Controlador: error al calcular cuota - " + e.getMessage());
            throw e;
        }
    }

    public double obtenerSaldoPendiente(int idPrestamo) {
        try {
            logger.info("Controlador: calculando saldo pendiente del préstamo " + idPrestamo);
            return prestamoServicio.calcularSaldoPendiente(idPrestamo);
        } catch (Exception e) {
            logger.warning("Controlador: error al calcular saldo - " + e.getMessage());
            throw e;
        }
    }

    // ─── Gestión de estados ───────────────────────────────────────────────────

    public void cancelar(int idPrestamo) {
        try {
            logger.info("Controlador: cancelando préstamo " + idPrestamo);
            prestamoServicio.cancelarPrestamo(idPrestamo);
        } catch (Exception e) {
            logger.warning("Controlador: error al cancelar préstamo - " + e.getMessage());
            throw e;
        }
    }

    public void marcarMoroso(int idPrestamo) {
        try {
            logger.info("Controlador: marcando como moroso préstamo " + idPrestamo);
            prestamoServicio.marcarComoMoroso(idPrestamo);
        } catch (Exception e) {
            logger.warning("Controlador: error al marcar moroso - " + e.getMessage());
            throw e;
        }
    }

    public void reactivar(int idPrestamo) {
        try {
            logger.info("Controlador: reactivando préstamo " + idPrestamo);
            prestamoServicio.reactivarPrestamo(idPrestamo);
        } catch (Exception e) {
            logger.warning("Controlador: error al reactivar préstamo - " + e.getMessage());
            throw e;
        }
    }

    public void verificarMorosidad(int idPrestamo) {
        try {
            logger.info("Controlador: verificando morosidad del préstamo " + idPrestamo);
            prestamoServicio.verificarMorosidad(idPrestamo);
        } catch (Exception e) {
            logger.warning("Controlador: error al verificar morosidad - " + e.getMessage());
            throw e;
        }
    }
}
