package co.edu.uptc.service;

import co.edu.uptc.model.*;
import co.edu.uptc.repository.IPrestamoRepositorio;
import co.edu.uptc.repository.ISocioRepositorio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class PrestamoServicio {

    private static final Logger logger = Logger.getLogger(PrestamoServicio.class.getName());

    private final IPrestamoRepositorio prestamoRepo;
    private final ISocioRepositorio socioRepo;

    public PrestamoServicio(IPrestamoRepositorio prestamoRepo, ISocioRepositorio socioRepo) {
        this.prestamoRepo = prestamoRepo;
        this.socioRepo = socioRepo;
    }

    // ─── CRUD básico ─────────────────────────────────────────────────────────

    /**
     * Registra un nuevo préstamo asociado a un socio.
     * Valida que el socio exista y que no supere el límite de 2 préstamos activos.
     */
    public Prestamo registrarPrestamo(int idSocio, double montoSolicitado,
                                      double tasaInteresAnual, int plazoMeses,
                                      LocalDate fechaInicio) {
        logger.info("Registrando préstamo para socio: " + idSocio);

        Socio socio = socioRepo.buscarPorId(idSocio)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado: " + idSocio));

        validarCondicionesParaPrestamo(socio, montoSolicitado, tasaInteresAnual, plazoMeses);

        Prestamo prestamo = new Prestamo(0, plazoMeses, montoSolicitado, tasaInteresAnual,
                EstadoPrestamo.ACTIVO, fechaInicio);
        prestamo.setPagos(new ArrayList<>());

        prestamoRepo.guardar(prestamo);

        // Vincular el préstamo al socio
        if (socio.getPrestamos() == null) {
            socio.setPrestamos(new ArrayList<>());
        }
        socio.getPrestamos().add(prestamo);
        socioRepo.actualizar(socio);

        logger.info("Préstamo registrado exitosamente con código: " + prestamo.getCodigo());
        return prestamo;
    }

    public Optional<Prestamo> buscarPorId(int id) {
        logger.info("Buscando préstamo con ID: " + id);
        return prestamoRepo.buscarPorId(id);
    }

    public List<Prestamo> listarTodos() {
        logger.info("Listando todos los préstamos");
        return prestamoRepo.listarTodos();
    }

    public List<Prestamo> listarPorSocio(int idSocio) {
        logger.info("Listando préstamos del socio: " + idSocio);
        Socio socio = socioRepo.buscarPorId(idSocio)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado: " + idSocio));
        return socio.getPrestamos() != null ? socio.getPrestamos() : new ArrayList<>();
    }

    public void actualizarPrestamo(Prestamo prestamo) {
        logger.info("Actualizando préstamo: " + prestamo.getCodigo());
        prestamoRepo.buscarPorId(prestamo.getCodigo())
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado: " + prestamo.getCodigo()));
        prestamoRepo.actualizar(prestamo);
        logger.info("Préstamo actualizado exitosamente: " + prestamo.getCodigo());
    }

    public void eliminarPrestamo(int id) {
        logger.info("Eliminando préstamo: " + id);
        prestamoRepo.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado: " + id));
        prestamoRepo.eliminar(id);
        logger.info("Préstamo eliminado: " + id);
    }

    // ─── Lógica financiera ────────────────────────────────────────────────────

    /**
     * Genera la tabla de amortización completa (sistema francés: cuota fija).
     * Fórmula: C = P * i / (1 - (1+i)^-n)
     *   donde i = tasa mensual, n = plazo en meses, P = monto solicitado.
     */
    public List<CuotaAmortizacion> generarTablaAmortizacion(int idPrestamo) {
        logger.info("Generando tabla de amortización para préstamo: " + idPrestamo);

        Prestamo prestamo = prestamoRepo.buscarPorId(idPrestamo)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado: " + idPrestamo));

        return calcularTablaAmortizacion(prestamo);
    }

    /**
     * Calcula la tabla de amortización a partir del objeto Prestamo (sin necesidad de ID).
     */
    public List<CuotaAmortizacion> calcularTablaAmortizacion(Prestamo prestamo) {
        double monto = prestamo.getMontoSolicitado();
        double tasaAnual = prestamo.getTasaInteresAnual();
        int plazo = prestamo.getPlazoMeses();
        LocalDate fechaInicio = prestamo.getFechaInicio();

        double tasaMensual = tasaAnual / 100.0 / 12.0;

        // Cuota fija mensual (sistema francés)
        double cuotaFija;
        if (tasaMensual == 0) {
            cuotaFija = monto / plazo;
        } else {
            cuotaFija = monto * tasaMensual / (1 - Math.pow(1 + tasaMensual, -plazo));
        }

        List<CuotaAmortizacion> tabla = new ArrayList<>();
        double saldo = monto;

        for (int i = 1; i <= plazo; i++) {
            double interesMes = saldo * tasaMensual;
            double capitalMes = cuotaFija - interesMes;
            double saldoFinal = saldo - capitalMes;

            // Evitar saldo negativo por redondeo en la última cuota
            if (i == plazo) {
                capitalMes = saldo;
                cuotaFija = capitalMes + interesMes;
                saldoFinal = 0;
            }

            LocalDate fechaVencimiento = fechaInicio.plusMonths(i);

            // Determinar si la cuota ya fue pagada (verificando pagos registrados)
            boolean pagada = esCuotaPagada(prestamo, i);

            CuotaAmortizacion cuota = new CuotaAmortizacion(
                    i,
                    Math.round(cuotaFija * 100.0) / 100.0,
                    Math.round(capitalMes * 100.0) / 100.0,
                    Math.round(interesMes * 100.0) / 100.0,
                    Math.round(saldo * 100.0) / 100.0,
                    Math.round(saldoFinal * 100.0) / 100.0,
                    pagada,
                    fechaVencimiento
            );

            tabla.add(cuota);
            saldo = saldoFinal;
        }

        logger.info("Tabla de amortización generada con " + tabla.size() + " cuotas");
        return tabla;
    }

    /**
     * Calcula el interés acumulado total pagado hasta el momento para un préstamo.
     */
    public double calcularInteresAcumulado(Prestamo prestamo) {
        if (prestamo.getPagos() == null || prestamo.getPagos().isEmpty()) {
            return 0;
        }
        return prestamo.getPagos().stream()
                .mapToDouble(Pago::getInteres)
                .sum();
    }

    /**
     * Calcula el saldo pendiente actual de un préstamo.
     */
    public double calcularSaldoPendiente(int idPrestamo) {
        Prestamo prestamo = prestamoRepo.buscarPorId(idPrestamo)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado: " + idPrestamo));

        double capitalPagado = prestamo.getPagos() == null ? 0 :
                prestamo.getPagos().stream().mapToDouble(Pago::getCapital).sum();

        return prestamo.getMontoSolicitado() - capitalPagado;
    }

    /**
     * Calcula la cuota mensual fija de un préstamo.
     */
    public double calcularCuotaMensual(Prestamo prestamo) {
        double monto = prestamo.getMontoSolicitado();
        double tasaMensual = prestamo.getTasaInteresAnual() / 100.0 / 12.0;
        int plazo = prestamo.getPlazoMeses();

        if (tasaMensual == 0) return monto / plazo;

        return monto * tasaMensual / (1 - Math.pow(1 + tasaMensual, -plazo));
    }

    // ─── Gestión de estados ───────────────────────────────────────────────────

    /**
     * Marca un préstamo como CANCELADO (totalmente pagado).
     */
    public void cancelarPrestamo(int idPrestamo) {
        logger.info("Cancelando préstamo: " + idPrestamo);
        Prestamo prestamo = prestamoRepo.buscarPorId(idPrestamo)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado: " + idPrestamo));

        if (prestamo.getEstado() == EstadoPrestamo.CANCELADO) {
            throw new IllegalStateException("El préstamo ya está cancelado");
        }

        prestamo.setEstado(EstadoPrestamo.CANCELADO);
        prestamoRepo.actualizar(prestamo);
        logger.info("Préstamo cancelado: " + idPrestamo);
    }

    /**
     * Marca un préstamo como MOROSO.
     */
    public void marcarComoMoroso(int idPrestamo) {
        logger.info("Marcando como moroso el préstamo: " + idPrestamo);
        Prestamo prestamo = prestamoRepo.buscarPorId(idPrestamo)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado: " + idPrestamo));

        if (prestamo.getEstado() == EstadoPrestamo.CANCELADO) {
            throw new IllegalStateException("No se puede marcar como moroso un préstamo cancelado");
        }

        prestamo.setEstado(EstadoPrestamo.MOROSO);
        prestamoRepo.actualizar(prestamo);
        logger.info("Préstamo marcado como moroso: " + idPrestamo);
    }

    /**
     * Reactiva un préstamo moroso a ACTIVO (si regulariza pagos).
     */
    public void reactivarPrestamo(int idPrestamo) {
        logger.info("Reactivando préstamo: " + idPrestamo);
        Prestamo prestamo = prestamoRepo.buscarPorId(idPrestamo)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado: " + idPrestamo));

        if (prestamo.getEstado() != EstadoPrestamo.MOROSO) {
            throw new IllegalStateException("Solo se pueden reactivar préstamos en estado MOROSO");
        }

        prestamo.setEstado(EstadoPrestamo.ACTIVO);
        prestamoRepo.actualizar(prestamo);
        logger.info("Préstamo reactivado: " + idPrestamo);
    }

    /**
     * Verifica automáticamente si algún préstamo activo debe pasar a MOROSO
     * (cuando hay cuotas vencidas sin pagar).
     */
    public void verificarMorosidad(int idPrestamo) {
        logger.info("Verificando morosidad del préstamo: " + idPrestamo);
        Prestamo prestamo = prestamoRepo.buscarPorId(idPrestamo)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado: " + idPrestamo));

        if (prestamo.getEstado() != EstadoPrestamo.ACTIVO) return;

        List<CuotaAmortizacion> tabla = calcularTablaAmortizacion(prestamo);
        LocalDate hoy = LocalDate.now();

        boolean tieneCuotaVencida = tabla.stream()
                .anyMatch(c -> !c.isPagada() && c.getFechaVencimiento().isBefore(hoy));

        if (tieneCuotaVencida) {
            prestamo.setEstado(EstadoPrestamo.MOROSO);
            prestamoRepo.actualizar(prestamo);
            logger.info("Préstamo " + idPrestamo + " marcado como MOROSO por cuotas vencidas");
        }
    }

    // ─── Validaciones ─────────────────────────────────────────────────────────

    private void validarCondicionesParaPrestamo(Socio socio, double monto,
                                                 double tasaInteresAnual, int plazoMeses) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto solicitado debe ser mayor a 0");
        }
        if (tasaInteresAnual < 0) {
            throw new IllegalArgumentException("La tasa de interés no puede ser negativa");
        }
        if (plazoMeses <= 0) {
            throw new IllegalArgumentException("El plazo en meses debe ser mayor a 0");
        }

        // Regla de negocio: no más de 2 préstamos activos por socio
        if (socio.getPrestamos() != null) {
            long activos = socio.getPrestamos().stream()
                    .filter(p -> p.getEstado() == EstadoPrestamo.ACTIVO)
                    .count();
            if (activos >= 2) {
                throw new IllegalArgumentException(
                        "El socio ya tiene 2 préstamos activos. No puede solicitar otro.");
            }
        }

        // Regla de negocio: cuota mensual no debe superar el 30% de ingresos
        double cuota = calcularCuotaMensualPreview(monto, tasaInteresAnual, plazoMeses);
        double limiteCapacidad = socio.getIngresosMensuales() * 0.30;
        if (cuota > limiteCapacidad) {
            throw new IllegalArgumentException(String.format(
                    "La cuota mensual ($%.2f) supera el 30%% de los ingresos del socio ($%.2f)",
                    cuota, limiteCapacidad));
        }
    }

    private double calcularCuotaMensualPreview(double monto, double tasaAnual, int plazo) {
        double tasaMensual = tasaAnual / 100.0 / 12.0;
        if (tasaMensual == 0) return monto / plazo;
        return monto * tasaMensual / (1 - Math.pow(1 + tasaMensual, -plazo));
    }

    private boolean esCuotaPagada(Prestamo prestamo, int numeroCuota) {
        if (prestamo.getPagos() == null) return false;
        // La cuota N está pagada si hay al menos N pagos registrados
        return prestamo.getPagos().size() >= numeroCuota;
    }
}
