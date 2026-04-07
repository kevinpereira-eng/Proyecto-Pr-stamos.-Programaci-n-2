package co.edu.uptc.service;

import co.edu.uptc.model.*;
import co.edu.uptc.repository.IPrestamoRepositorio;
import co.edu.uptc.repository.ISocioRepositorio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PagoServicio {

    private static final Logger logger = Logger.getLogger(PagoServicio.class.getName());

    private final IPrestamoRepositorio prestamoRepo;
    private final ISocioRepositorio socioRepo;
    private final PrestamoServicio prestamoServicio;

    public PagoServicio(IPrestamoRepositorio prestamoRepo, ISocioRepositorio socioRepo,
                        PrestamoServicio prestamoServicio) {
        this.prestamoRepo = prestamoRepo;
        this.socioRepo = socioRepo;
        this.prestamoServicio = prestamoServicio;
    }

    // ─── Registro de pagos ────────────────────────────────────────────────────

    /**
     * Registra el pago de la próxima cuota de un préstamo.
     * Calcula automáticamente capital e interés según la tabla de amortización.
     * @return el Pago registrado
     */
    public Pago registrarPago(int idPrestamo, LocalDate fechaPago) {
        logger.info("Registrando pago para préstamo: " + idPrestamo);

        Prestamo prestamo = prestamoRepo.buscarPorId(idPrestamo)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado: " + idPrestamo));

        validarEstadoParaPago(prestamo);

        // Obtener la tabla de amortización y la siguiente cuota a pagar
        List<CuotaAmortizacion> tabla = prestamoServicio.calcularTablaAmortizacion(prestamo);
        int cuotasPagadas = prestamo.getPagos() == null ? 0 : prestamo.getPagos().size();

        if (cuotasPagadas >= prestamo.getPlazoMeses()) {
            throw new IllegalStateException("El préstamo ya está completamente pagado");
        }

        CuotaAmortizacion cuotaActual = tabla.get(cuotasPagadas);

        double saldoInicial = cuotaActual.getSaldoInicial();
        double interes = cuotaActual.getInteres();
        double capital = cuotaActual.getCapital();
        double montoCuota = cuotaActual.getCuotaTotal();
        double saldoFinal = cuotaActual.getSalgoFinal();

        // Crear el pago
        Pago pago = new Pago(0, montoCuota, capital, interes, saldoInicial, saldoFinal, fechaPago);

        // Agregar el pago al préstamo
        if (prestamo.getPagos() == null) {
            prestamo.setPagos(new ArrayList<>());
        }
        prestamo.getPagos().add(pago);

        // Si se pagó la última cuota, cambiar estado a CANCELADO
        if (prestamo.getPagos().size() == prestamo.getPlazoMeses()) {
            prestamo.setEstado(EstadoPrestamo.CANCELADO);
            logger.info("Préstamo " + idPrestamo + " completamente pagado → CANCELADO");
        } else if (prestamo.getEstado() == EstadoPrestamo.MOROSO) {
            // Si estaba moroso y paga, vuelve a ACTIVO
            prestamo.setEstado(EstadoPrestamo.ACTIVO);
            logger.info("Préstamo " + idPrestamo + " regularizado → ACTIVO");
        }

        prestamoRepo.actualizar(prestamo);
        // Sincronizar también en el socio
        sincronizarPrestamoEnSocio(prestamo);

        logger.info(String.format("Pago registrado: cuota %d, monto=%.2f, capital=%.2f, interés=%.2f",
                cuotasPagadas + 1, montoCuota, capital, interes));

        return pago;
    }

    /**
     * Registra un pago con monto personalizado (pago anticipado / abono extra).
     * Se aplica primero a intereses y el resto a capital.
     */
    public Pago registrarPagoAnticipado(int idPrestamo, double montoPagado, LocalDate fechaPago) {
        logger.info("Registrando pago anticipado para préstamo: " + idPrestamo + ", monto: " + montoPagado);

        Prestamo prestamo = prestamoRepo.buscarPorId(idPrestamo)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado: " + idPrestamo));

        validarEstadoParaPago(prestamo);

        if (montoPagado <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a 0");
        }

        double saldoActual = prestamoServicio.calcularSaldoPendiente(idPrestamo);

        if (montoPagado > saldoActual * 1.01) { // tolerancia del 1% por redondeo
            throw new IllegalArgumentException(String.format(
                    "El monto pagado (%.2f) supera el saldo pendiente (%.2f)", montoPagado, saldoActual));
        }

        // Calcular interés del período actual
        double tasaMensual = prestamo.getTasaInteresAnual() / 100.0 / 12.0;
        double interes = saldoActual * tasaMensual;
        double capital = Math.min(montoPagado - interes, saldoActual);
        if (capital < 0) capital = 0;
        double saldoFinal = saldoActual - capital;

        Pago pago = new Pago(0, montoPagado, capital, interes, saldoActual, saldoFinal, fechaPago);

        if (prestamo.getPagos() == null) {
            prestamo.setPagos(new ArrayList<>());
        }
        prestamo.getPagos().add(pago);

        if (saldoFinal <= 0.01) {
            prestamo.setEstado(EstadoPrestamo.CANCELADO);
            logger.info("Préstamo " + idPrestamo + " cancelado por pago anticipado total");
        }

        prestamoRepo.actualizar(prestamo);
        sincronizarPrestamoEnSocio(prestamo);

        return pago;
    }

    // ─── Consulta de pagos ────────────────────────────────────────────────────

    /**
     * Retorna el historial de pagos de un préstamo.
     */
    public List<Pago> listarPagosPorPrestamo(int idPrestamo) {
        logger.info("Listando pagos del préstamo: " + idPrestamo);
        Prestamo prestamo = prestamoRepo.buscarPorId(idPrestamo)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado: " + idPrestamo));
        return prestamo.getPagos() != null ? prestamo.getPagos() : new ArrayList<>();
    }

    /**
     * Obtiene la próxima cuota a pagar (fecha y monto) de un préstamo.
     */
    public CuotaAmortizacion obtenerProximaCuota(int idPrestamo) {
        logger.info("Obteniendo próxima cuota del préstamo: " + idPrestamo);

        Prestamo prestamo = prestamoRepo.buscarPorId(idPrestamo)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado: " + idPrestamo));

        if (prestamo.getEstado() == EstadoPrestamo.CANCELADO) {
            throw new IllegalStateException("El préstamo ya está cancelado");
        }

        List<CuotaAmortizacion> tabla = prestamoServicio.calcularTablaAmortizacion(prestamo);
        int cuotasPagadas = prestamo.getPagos() == null ? 0 : prestamo.getPagos().size();

        if (cuotasPagadas >= tabla.size()) {
            throw new IllegalStateException("No hay cuotas pendientes");
        }

        return tabla.get(cuotasPagadas);
    }

    /**
     * Calcula el total de intereses pagados hasta el momento para un préstamo.
     */
    public double calcularTotalInteresesPagados(int idPrestamo) {
        Prestamo prestamo = prestamoRepo.buscarPorId(idPrestamo)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado: " + idPrestamo));
        return prestamoServicio.calcularInteresAcumulado(prestamo);
    }

    /**
     * Calcula el total de capital abonado hasta el momento.
     */
    public double calcularTotalCapitalPagado(int idPrestamo) {
        Prestamo prestamo = prestamoRepo.buscarPorId(idPrestamo)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado: " + idPrestamo));

        if (prestamo.getPagos() == null || prestamo.getPagos().isEmpty()) return 0;
        return prestamo.getPagos().stream().mapToDouble(Pago::getCapital).sum();
    }

    // ─── Validaciones ─────────────────────────────────────────────────────────

    private void validarEstadoParaPago(Prestamo prestamo) {
        if (prestamo.getEstado() == EstadoPrestamo.CANCELADO) {
            throw new IllegalStateException("No se pueden registrar pagos en un préstamo cancelado");
        }
    }

    // ─── Sincronización Socio ─────────────────────────────────────────────────

    /**
     * Actualiza el préstamo dentro del objeto Socio para mantener consistencia.
     */
    private void sincronizarPrestamoEnSocio(Prestamo prestamo) {
        // Buscar al socio que tiene este préstamo y actualizarlo
        List<?> socios = socioRepo.listarTodos();
        for (Object obj : socios) {
            if (obj instanceof Socio socio) {
                if (socio.getPrestamos() == null) continue;
                boolean encontrado = false;
                for (int i = 0; i < socio.getPrestamos().size(); i++) {
                    if (socio.getPrestamos().get(i).getCodigo() == prestamo.getCodigo()) {
                        socio.getPrestamos().set(i, prestamo);
                        encontrado = true;
                        break;
                    }
                }
                if (encontrado) {
                    socioRepo.actualizar(socio);
                    break;
                }
            }
        }
    }
}
