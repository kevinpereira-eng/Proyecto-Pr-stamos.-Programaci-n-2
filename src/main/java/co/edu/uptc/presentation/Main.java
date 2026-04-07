package co.edu.uptc.presentation;

import co.edu.uptc.controller.PagoControlador;
import co.edu.uptc.controller.PrestamoControlador;
import co.edu.uptc.controller.ReporteControlador;
import co.edu.uptc.controller.SocioControlador;
import co.edu.uptc.model.*;
import co.edu.uptc.repository.PrestamoRepositorioJson;
import co.edu.uptc.repository.SocioRepositorioJson;
import co.edu.uptc.service.*;
import co.edu.uptc.util.ExportadorReporte;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class Main {

    // ── Controladores ──────────────────────────────────────────────────────────
    private static SocioControlador socioControlador;
    private static PrestamoControlador prestamoControlador;
    private static PagoControlador pagoControlador;
    private static ReporteControlador reporteControlador;

    public static void main(String[] args) {
        inicializarDependencias();
        menuPrincipal();
    }

    // ── Inicialización de capas ────────────────────────────────────────────────
    private static void inicializarDependencias() {
        String recursos = "src/main/java/co/edu/uptc/resources/";
        SocioRepositorioJson socioRepo = new SocioRepositorioJson(recursos + "socios.json");
        PrestamoRepositorioJson prestamoRepo = new PrestamoRepositorioJson(recursos + "prestamos.json");
        ExportadorReporte exportador = new ExportadorReporte();

        SocioServicio socioServicio = new SocioServicio(socioRepo);
        PrestamoServicio prestamoServicio = new PrestamoServicio(prestamoRepo, socioRepo);
        PagoServicio pagoServicio = new PagoServicio(prestamoRepo, socioRepo, prestamoServicio);
        ReporteServicio reporteServicio = new ReporteServicio(prestamoRepo, socioRepo, exportador);

        socioControlador = new SocioControlador(socioServicio);
        prestamoControlador = new PrestamoControlador(prestamoServicio);
        pagoControlador = new PagoControlador(pagoServicio);
        reporteControlador = new ReporteControlador(reporteServicio);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MENÚ PRINCIPAL
    // ══════════════════════════════════════════════════════════════════════════
    private static void menuPrincipal() {
        String[] opciones = {
            "👤  Gestión de Socios",
            "💰  Gestión de Préstamos",
            "💳  Gestión de Pagos",
            "📊  Reportes",
            "❌  Salir"
        };

        while (true) {
            int seleccion = JOptionPane.showOptionDialog(
                null,
                "Bienvenido al Sistema de Gestión de Préstamos\nCooperativa Financiera Progreso",
                "MENÚ PRINCIPAL",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                opciones,
                opciones[0]
            );

            switch (seleccion) {
                case 0 -> menuSocios();
                case 1 -> menuPrestamos();
                case 2 -> menuPagos();
                case 3 -> menuReportes();
                case 4, -1 -> {
                    JOptionPane.showMessageDialog(null,
                        "Gracias por usar el sistema.\n¡Hasta pronto!",
                        "Salir", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MENÚ SOCIOS
    // ══════════════════════════════════════════════════════════════════════════
    private static void menuSocios() {
        String[] opciones = {
            "➕  Registrar socio",
            "🔍  Consultar socio por ID",
            "📋  Listar todos los socios",
            "✏️   Modificar socio",
            "🗑️   Eliminar socio",
            "🔙  Volver"
        };

        while (true) {
            int sel = JOptionPane.showOptionDialog(null,
                "Seleccione una opción:", "GESTIÓN DE SOCIOS",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, opciones, opciones[0]);

            switch (sel) {
                case 0 -> registrarSocio();
                case 1 -> consultarSocio();
                case 2 -> listarSocios();
                case 3 -> modificarSocio();
                case 4 -> eliminarSocio();
                case 5, -1 -> { return; }
            }
        }
    }

    private static void registrarSocio() {
        try {
            String nombre = pedirTexto("Nombre del socio:", "Registrar Socio");
            if (nombre == null) return;

            String correo = pedirTexto("Correo electrónico:", "Registrar Socio");
            if (correo == null) return;

            String ingresosStr = pedirTexto("Ingresos mensuales ($):", "Registrar Socio");
            if (ingresosStr == null) return;
            double ingresos = Double.parseDouble(ingresosStr);

            String[] niveles = {"BAJO", "MEDIO", "ALTO"};
            int nivelIdx = JOptionPane.showOptionDialog(null,
                "Seleccione el nivel de riesgo:", "Nivel de Riesgo",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, niveles, niveles[0]);
            if (nivelIdx == -1) return;
            NivelRiesgo nivel = NivelRiesgo.values()[nivelIdx];

            Socio nuevo = new Socio("", nombre, correo, ingresos, nivel);
            socioControlador.registrar(nuevo);

            mostrarExito("Socio registrado exitosamente.\nID asignado: " + nuevo.getId());

        } catch (NumberFormatException e) {
            mostrarError("El valor de ingresos ingresado no es válido.");
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    private static void consultarSocio() {
        String idStr = pedirTexto("Ingrese el ID del socio:", "Consultar Socio");
        if (idStr == null) return;
        try {
            int id = Integer.parseInt(idStr);
            socioControlador.consultar(id).ifPresentOrElse(
                s -> mostrarInfo(formatearSocio(s), "Datos del Socio"),
                () -> mostrarError("Socio con ID " + id + " no encontrado.")
            );
        } catch (NumberFormatException e) {
            mostrarError("ID inválido.");
        }
    }

    private static void listarSocios() {
        List<Socio> socios = socioControlador.listar();
        if (socios.isEmpty()) {
            mostrarInfo("No hay socios registrados.", "Lista de Socios");
            return;
        }
        StringBuilder sb = new StringBuilder("═══ SOCIOS REGISTRADOS ═══\n\n");
        for (Socio s : socios) {
            sb.append(formatearSocioResumen(s)).append("\n");
            sb.append("─────────────────────────\n");
        }
        mostrarScrollable(sb.toString(), "Lista de Socios (" + socios.size() + ")");
    }

    private static void modificarSocio() {
        String idStr = pedirTexto("ID del socio a modificar:", "Modificar Socio");
        if (idStr == null) return;
        try {
            int id = Integer.parseInt(idStr);
            socioControlador.consultar(id).ifPresentOrElse(s -> {
                try {
                    String nombre = pedirTextoConDefault("Nuevo nombre:", s.getNombre(), "Modificar Socio");
                    if (nombre == null) return;

                    String correo = pedirTextoConDefault("Nuevo correo:", s.getCorreo(), "Modificar Socio");
                    if (correo == null) return;

                    String ingresosStr = pedirTextoConDefault("Nuevos ingresos mensuales ($):",
                        String.valueOf(s.getIngresosMensuales()), "Modificar Socio");
                    if (ingresosStr == null) return;

                    String[] niveles = {"BAJO", "MEDIO", "ALTO"};
                    int nivelIdx = JOptionPane.showOptionDialog(null,
                        "Nuevo nivel de riesgo:", "Modificar Socio",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, niveles, s.getNivelRiesgo().name());
                    if (nivelIdx == -1) return;

                    s.setNombre(nombre.isBlank() ? s.getNombre() : nombre);
                    s.setCorreo(correo.isBlank() ? s.getCorreo() : correo);
                    s.setIngresosMensuales(ingresosStr.isBlank()
                        ? s.getIngresosMensuales()
                        : Double.parseDouble(ingresosStr));
                    s.setNivelRiesgo(NivelRiesgo.values()[nivelIdx]);

                    socioControlador.modificar(s);
                    mostrarExito("Socio modificado exitosamente.");
                } catch (NumberFormatException e) {
                    mostrarError("Valor de ingresos inválido.");
                } catch (IllegalArgumentException e) {
                    mostrarError(e.getMessage());
                }
            }, () -> mostrarError("Socio no encontrado."));
        } catch (NumberFormatException e) {
            mostrarError("ID inválido.");
        }
    }

    private static void eliminarSocio() {
        String idStr = pedirTexto("ID del socio a eliminar:", "Eliminar Socio");
        if (idStr == null) return;
        try {
            int id = Integer.parseInt(idStr);
            int confirm = JOptionPane.showConfirmDialog(null,
                "¿Está seguro que desea eliminar el socio con ID " + id + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            socioControlador.eliminar(id);
            mostrarExito("Socio eliminado correctamente.");
        } catch (NumberFormatException e) {
            mostrarError("ID inválido.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MENÚ PRÉSTAMOS
    // ══════════════════════════════════════════════════════════════════════════
    private static void menuPrestamos() {
        String[] opciones = {
            "➕  Registrar préstamo",
            "🔍  Consultar préstamo por ID",
            "📋  Listar todos los préstamos",
            "📋  Listar préstamos de un socio",
            "📊  Ver tabla de amortización",
            "💵  Consultar saldo pendiente",
            "❌  Cancelar préstamo",
            "⚠️   Marcar como moroso",
            "✅  Reactivar préstamo",
            "🔄  Verificar morosidad",
            "🗑️   Eliminar préstamo",
            "🔙  Volver"
        };

        while (true) {
            int sel = JOptionPane.showOptionDialog(null,
                "Seleccione una opción:", "GESTIÓN DE PRÉSTAMOS",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, opciones, opciones[0]);

            switch (sel) {
                case 0  -> registrarPrestamo();
                case 1  -> consultarPrestamo();
                case 2  -> listarPrestamos();
                case 3  -> listarPrestamosPorSocio();
                case 4  -> verTablaAmortizacion();
                case 5  -> consultarSaldoPendiente();
                case 6  -> cancelarPrestamo();
                case 7  -> marcarMoroso();
                case 8  -> reactivarPrestamo();
                case 9  -> verificarMorosidad();
                case 10 -> eliminarPrestamo();
                case 11, -1 -> { return; }
            }
        }
    }

    private static void registrarPrestamo() {
        try {
            String idSocioStr = pedirTexto("ID del socio:", "Registrar Préstamo");
            if (idSocioStr == null) return;
            int idSocio = Integer.parseInt(idSocioStr);

            String montoStr = pedirTexto("Monto solicitado ($):", "Registrar Préstamo");
            if (montoStr == null) return;
            double monto = Double.parseDouble(montoStr);

            String tasaStr = pedirTexto("Tasa de interés anual (%):", "Registrar Préstamo");
            if (tasaStr == null) return;
            double tasa = Double.parseDouble(tasaStr);

            String plazoStr = pedirTexto("Plazo en meses:", "Registrar Préstamo");
            if (plazoStr == null) return;
            int plazo = Integer.parseInt(plazoStr);

            String fechaStr = pedirTexto("Fecha de inicio (yyyy-MM-dd):", "Registrar Préstamo");
            if (fechaStr == null) return;
            LocalDate fechaInicio = LocalDate.parse(fechaStr);

            Prestamo p = prestamoControlador.registrar(idSocio, monto, tasa, plazo, fechaInicio);

            double cuota = prestamoControlador.obtenerCuotaMensual(p.getCodigo());
            mostrarExito(String.format(
                "Préstamo registrado exitosamente.\n" +
                "Código: %d\n" +
                "Cuota mensual calculada: $%.2f",
                p.getCodigo(), cuota));

        } catch (NumberFormatException e) {
            mostrarError("Valor numérico inválido.");
        } catch (DateTimeParseException e) {
            mostrarError("Formato de fecha inválido. Use yyyy-MM-dd.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private static void consultarPrestamo() {
        String idStr = pedirTexto("ID del préstamo:", "Consultar Préstamo");
        if (idStr == null) return;
        try {
            int id = Integer.parseInt(idStr);
            prestamoControlador.consultar(id).ifPresentOrElse(
                p -> mostrarInfo(formatearPrestamo(p), "Datos del Préstamo"),
                () -> mostrarError("Préstamo con ID " + id + " no encontrado.")
            );
        } catch (NumberFormatException e) {
            mostrarError("ID inválido.");
        }
    }

    private static void listarPrestamos() {
        List<Prestamo> prestamos = prestamoControlador.listar();
        if (prestamos.isEmpty()) {
            mostrarInfo("No hay préstamos registrados.", "Lista de Préstamos");
            return;
        }
        StringBuilder sb = new StringBuilder("═══ PRÉSTAMOS REGISTRADOS ═══\n\n");
        for (Prestamo p : prestamos) {
            sb.append(formatearPrestamoResumen(p)).append("\n");
            sb.append("─────────────────────────\n");
        }
        mostrarScrollable(sb.toString(), "Préstamos (" + prestamos.size() + ")");
    }

    private static void listarPrestamosPorSocio() {
        String idStr = pedirTexto("ID del socio:", "Préstamos por Socio");
        if (idStr == null) return;
        try {
            int idSocio = Integer.parseInt(idStr);
            List<Prestamo> prestamos = prestamoControlador.listarPorSocio(idSocio);
            if (prestamos.isEmpty()) {
                mostrarInfo("El socio no tiene préstamos registrados.", "Préstamos del Socio");
                return;
            }
            StringBuilder sb = new StringBuilder("═══ PRÉSTAMOS DEL SOCIO " + idSocio + " ═══\n\n");
            for (Prestamo p : prestamos) {
                sb.append(formatearPrestamoResumen(p)).append("\n");
                sb.append("─────────────────────────\n");
            }
            mostrarScrollable(sb.toString(), "Préstamos del Socio");
        } catch (NumberFormatException e) {
            mostrarError("ID inválido.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private static void verTablaAmortizacion() {
        String idStr = pedirTexto("ID del préstamo:", "Tabla de Amortización");
        if (idStr == null) return;
        try {
            int id = Integer.parseInt(idStr);
            List<CuotaAmortizacion> tabla = prestamoControlador.obtenerTablaAmortizacion(id);

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-6s %-12s %-12s %-12s %-14s %-14s %-10s%n",
                "Cuota", "Total ($)", "Capital ($)", "Interés ($)", "Saldo Ini ($)", "Saldo Fin ($)", "Estado"));
            sb.append("─".repeat(82)).append("\n");

            for (CuotaAmortizacion c : tabla) {
                sb.append(String.format("%-6d %-12.2f %-12.2f %-12.2f %-14.2f %-14.2f %-10s%n",
                    c.getNimeroCuota(),
                    c.getCuotaTotal(),
                    c.getCapital(),
                    c.getInteres(),
                    c.getSaldoInicial(),
                    c.getSalgoFinal(),
                    c.isPagada() ? "✅ Pagada" : "⏳ Pendiente"));
            }

            mostrarScrollable(sb.toString(), "Tabla de Amortización — Préstamo #" + id);
        } catch (NumberFormatException e) {
            mostrarError("ID inválido.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private static void consultarSaldoPendiente() {
        String idStr = pedirTexto("ID del préstamo:", "Saldo Pendiente");
        if (idStr == null) return;
        try {
            int id = Integer.parseInt(idStr);
            double saldo = prestamoControlador.obtenerSaldoPendiente(id);
            mostrarInfo(String.format("Saldo pendiente del préstamo #%d:\n\n$ %.2f", id, saldo),
                "Saldo Pendiente");
        } catch (NumberFormatException e) {
            mostrarError("ID inválido.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private static void cancelarPrestamo() {
        String idStr = pedirTexto("ID del préstamo a cancelar:", "Cancelar Préstamo");
        if (idStr == null) return;
        try {
            int id = Integer.parseInt(idStr);
            int confirm = JOptionPane.showConfirmDialog(null,
                "¿Desea marcar el préstamo #" + id + " como CANCELADO?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
            prestamoControlador.cancelar(id);
            mostrarExito("Préstamo #" + id + " cancelado exitosamente.");
        } catch (NumberFormatException e) {
            mostrarError("ID inválido.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private static void marcarMoroso() {
        String idStr = pedirTexto("ID del préstamo:", "Marcar como Moroso");
        if (idStr == null) return;
        try {
            int id = Integer.parseInt(idStr);
            prestamoControlador.marcarMoroso(id);
            mostrarExito("Préstamo #" + id + " marcado como MOROSO.");
        } catch (NumberFormatException e) {
            mostrarError("ID inválido.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private static void reactivarPrestamo() {
        String idStr = pedirTexto("ID del préstamo a reactivar:", "Reactivar Préstamo");
        if (idStr == null) return;
        try {
            int id = Integer.parseInt(idStr);
            prestamoControlador.reactivar(id);
            mostrarExito("Préstamo #" + id + " reactivado a ACTIVO.");
        } catch (NumberFormatException e) {
            mostrarError("ID inválido.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private static void verificarMorosidad() {
        String idStr = pedirTexto("ID del préstamo a verificar:", "Verificar Morosidad");
        if (idStr == null) return;
        try {
            int id = Integer.parseInt(idStr);
            prestamoControlador.verificarMorosidad(id);
            mostrarExito("Verificación de morosidad completada para préstamo #" + id + ".\nConsulte el estado actualizado.");
        } catch (NumberFormatException e) {
            mostrarError("ID inválido.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private static void eliminarPrestamo() {
        String idStr = pedirTexto("ID del préstamo a eliminar:", "Eliminar Préstamo");
        if (idStr == null) return;
        try {
            int id = Integer.parseInt(idStr);
            int confirm = JOptionPane.showConfirmDialog(null,
                "¿Está seguro que desea eliminar el préstamo #" + id + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
            prestamoControlador.eliminar(id);
            mostrarExito("Préstamo #" + id + " eliminado correctamente.");
        } catch (NumberFormatException e) {
            mostrarError("ID inválido.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MENÚ PAGOS
    // ══════════════════════════════════════════════════════════════════════════
    private static void menuPagos() {
        String[] opciones = {
            "💳  Registrar pago de cuota",
            "💸  Registrar pago anticipado",
            "📋  Ver historial de pagos",
            "📅  Ver próxima cuota",
            "📈  Ver total intereses pagados",
            "📉  Ver total capital pagado",
            "🔙  Volver"
        };

        while (true) {
            int sel = JOptionPane.showOptionDialog(null,
                "Seleccione una opción:", "GESTIÓN DE PAGOS",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, opciones, opciones[0]);

            switch (sel) {
                case 0 -> registrarPago();
                case 1 -> registrarPagoAnticipado();
                case 2 -> verHistorialPagos();
                case 3 -> verProximaCuota();
                case 4 -> verTotalInteresesPagados();
                case 5 -> verTotalCapitalPagado();
                case 6, -1 -> { return; }
            }
        }
    }

    private static void registrarPago() {
        try {
            String idStr = pedirTexto("ID del préstamo:", "Registrar Pago de Cuota");
            if (idStr == null) return;
            int id = Integer.parseInt(idStr);

            // Mostrar próxima cuota antes de confirmar
            CuotaAmortizacion proxima = pagoControlador.obtenerProximaCuota(id);
            int confirm = JOptionPane.showConfirmDialog(null,
                String.format("Próxima cuota a pagar:\n\n" +
                    "Cuota #%d\nMonto: $%.2f\nInterés: $%.2f\nCapital: $%.2f\nVence: %s\n\n¿Confirmar pago?",
                    proxima.getNimeroCuota(), proxima.getCuotaTotal(),
                    proxima.getInteres(), proxima.getCapital(), proxima.getFechaVencimiento()),
                "Confirmar Pago", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) return;

            String fechaStr = pedirTexto("Fecha del pago (yyyy-MM-dd):", "Registrar Pago");
            if (fechaStr == null) return;
            LocalDate fecha = LocalDate.parse(fechaStr);

            Pago pago = pagoControlador.registrarPago(id, fecha);
            mostrarExito(String.format(
                "Pago registrado exitosamente.\n\n" +
                "ID Pago: %d\nMonto pagado: $%.2f\nCapital: $%.2f\nInterés: $%.2f\nSaldo final: $%.2f",
                pago.getId(), pago.getMontoPagado(), pago.getCapital(),
                pago.getInteres(), pago.getSaldoFinal()));

        } catch (NumberFormatException e) {
            mostrarError("ID inválido.");
        } catch (DateTimeParseException e) {
            mostrarError("Formato de fecha inválido. Use yyyy-MM-dd.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private static void registrarPagoAnticipado() {
        try {
            String idStr = pedirTexto("ID del préstamo:", "Pago Anticipado");
            if (idStr == null) return;
            int id = Integer.parseInt(idStr);

            double saldo = prestamoControlador.obtenerSaldoPendiente(id);
            String montoStr = pedirTexto(
                String.format("Saldo pendiente: $%.2f\nMonto del pago anticipado ($):", saldo),
                "Pago Anticipado");
            if (montoStr == null) return;
            double monto = Double.parseDouble(montoStr);

            String fechaStr = pedirTexto("Fecha del pago (yyyy-MM-dd):", "Pago Anticipado");
            if (fechaStr == null) return;
            LocalDate fecha = LocalDate.parse(fechaStr);

            Pago pago = pagoControlador.registrarPagoAnticipado(id, monto, fecha);
            mostrarExito(String.format(
                "Pago anticipado registrado.\n\n" +
                "ID Pago: %d\nMonto: $%.2f\nCapital: $%.2f\nInterés: $%.2f\nNuevo saldo: $%.2f",
                pago.getId(), pago.getMontoPagado(), pago.getCapital(),
                pago.getInteres(), pago.getSaldoFinal()));

        } catch (NumberFormatException e) {
            mostrarError("Valor numérico inválido.");
        } catch (DateTimeParseException e) {
            mostrarError("Formato de fecha inválido. Use yyyy-MM-dd.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private static void verHistorialPagos() {
        String idStr = pedirTexto("ID del préstamo:", "Historial de Pagos");
        if (idStr == null) return;
        try {
            int id = Integer.parseInt(idStr);
            List<Pago> pagos = pagoControlador.listarPagos(id);

            if (pagos.isEmpty()) {
                mostrarInfo("No hay pagos registrados para este préstamo.", "Historial de Pagos");
                return;
            }

            StringBuilder sb = new StringBuilder("═══ HISTORIAL DE PAGOS — Préstamo #" + id + " ═══\n\n");
            sb.append(String.format("%-8s %-14s %-12s %-12s %-12s %-12s%n",
                "ID", "Fecha", "Monto ($)", "Capital ($)", "Interés ($)", "Saldo ($)"));
            sb.append("─".repeat(72)).append("\n");

            for (Pago p : pagos) {
                sb.append(String.format("%-8d %-14s %-12.2f %-12.2f %-12.2f %-12.2f%n",
                    p.getId(), p.getFechaPago(), p.getMontoPagado(),
                    p.getCapital(), p.getInteres(), p.getSaldoFinal()));
            }

            double totalPagado = pagos.stream().mapToDouble(Pago::getMontoPagado).sum();
            sb.append("\nTotal pagado hasta ahora: $").append(String.format("%.2f", totalPagado));

            mostrarScrollable(sb.toString(), "Historial de Pagos");
        } catch (NumberFormatException e) {
            mostrarError("ID inválido.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private static void verProximaCuota() {
        String idStr = pedirTexto("ID del préstamo:", "Próxima Cuota");
        if (idStr == null) return;
        try {
            int id = Integer.parseInt(idStr);
            CuotaAmortizacion c = pagoControlador.obtenerProximaCuota(id);
            mostrarInfo(String.format(
                "Próxima cuota del préstamo #%d:\n\n" +
                "Número de cuota: %d\n" +
                "Monto total: $%.2f\n" +
                "Capital: $%.2f\n" +
                "Interés: $%.2f\n" +
                "Saldo inicial: $%.2f\n" +
                "Saldo final: $%.2f\n" +
                "Fecha de vencimiento: %s",
                id, c.getNimeroCuota(), c.getCuotaTotal(), c.getCapital(),
                c.getInteres(), c.getSaldoInicial(), c.getSalgoFinal(),
                c.getFechaVencimiento()),
                "Próxima Cuota");
        } catch (NumberFormatException e) {
            mostrarError("ID inválido.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private static void verTotalInteresesPagados() {
        String idStr = pedirTexto("ID del préstamo:", "Total Intereses Pagados");
        if (idStr == null) return;
        try {
            int id = Integer.parseInt(idStr);
            double total = pagoControlador.obtenerTotalInteresesPagados(id);
            mostrarInfo(String.format("Total de intereses pagados\ndel préstamo #%d:\n\n$ %.2f", id, total),
                "Total Intereses");
        } catch (NumberFormatException e) {
            mostrarError("ID inválido.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private static void verTotalCapitalPagado() {
        String idStr = pedirTexto("ID del préstamo:", "Total Capital Pagado");
        if (idStr == null) return;
        try {
            int id = Integer.parseInt(idStr);
            double total = pagoControlador.obtenerTotalCapitalPagado(id);
            mostrarInfo(String.format("Total de capital pagado\ndel préstamo #%d:\n\n$ %.2f", id, total),
                "Total Capital");
        } catch (NumberFormatException e) {
            mostrarError("ID inválido.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MENÚ REPORTES
    // ══════════════════════════════════════════════════════════════════════════
    private static void menuReportes() {
        String[] opciones = {
            "📊  Reporte de ingresos por intereses",
            "🏆  Ranking de socios (Top 5)",
            "💾  Exportar reporte a JSON",
            "📄  Exportar reporte a CSV",
            "🔙  Volver"
        };

        while (true) {
            int sel = JOptionPane.showOptionDialog(null,
                "Seleccione un reporte:", "REPORTES",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, opciones, opciones[0]);

            switch (sel) {
                case 0 -> reporteIngresos();
                case 1 -> rankingSocios();
                case 2 -> exportarJson();
                case 3 -> exportarCsv();
                case 4, -1 -> { return; }
            }
        }
    }

    private static void reporteIngresos() {
        try {
            String inicioStr = pedirTexto("Fecha de inicio (yyyy-MM-dd):", "Reporte de Ingresos");
            if (inicioStr == null) return;
            String finStr = pedirTexto("Fecha de fin (yyyy-MM-dd):", "Reporte de Ingresos");
            if (finStr == null) return;

            LocalDate inicio = LocalDate.parse(inicioStr);
            LocalDate fin = LocalDate.parse(finStr);

            ReporteIngreso reporte = reporteControlador.generarReporteIngresosPorIntereses(inicio, fin);

            mostrarInfo(String.format(
                "═══ REPORTE DE INGRESOS ═══\n\n" +
                "Período: %s → %s\n\n" +
                "Total intereses recaudados: $%.2f\n" +
                "Préstamos activos:          %d\n" +
                "Fecha de generación:        %s",
                inicio, fin,
                reporte.getTotalInteresesRecaudados(),
                reporte.getTotalPrestamosActivos(),
                reporte.getGeneradoEn()),
                "Reporte de Ingresos");

        } catch (DateTimeParseException e) {
            mostrarError("Formato de fecha inválido. Use yyyy-MM-dd.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private static void rankingSocios() {
        try {
            List<RankingSocio> ranking = reporteControlador.rankingBeneficioSocios();

            if (ranking.isEmpty()) {
                mostrarInfo("No hay datos para generar el ranking.", "Ranking de Socios");
                return;
            }

            StringBuilder sb = new StringBuilder("🏆 TOP 5 — SOCIOS POR MONTO FINANCIADO 🏆\n\n");
            sb.append(String.format("%-5s %-8s %-25s %-18s %-12s%n",
                "Pos.", "ID", "Nombre", "Monto Total ($)", "Préstamos"));
            sb.append("─".repeat(72)).append("\n");

            int pos = 1;
            for (RankingSocio r : ranking) {
                sb.append(String.format("%-5d %-8s %-25s %-18.2f %-12d%n",
                    pos++, r.getIdSocio(), r.getNombreSocio(),
                    r.getMontoTotalFinanciado(), r.getCantidadPrestamos()));
            }

            mostrarScrollable(sb.toString(), "Ranking de Socios");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private static void exportarJson() {
        try {
            String[] tiposReporte = {"Ranking de socios", "Lista de préstamos"};
            int tipo = JOptionPane.showOptionDialog(null,
                "¿Qué datos desea exportar?", "Exportar JSON",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, tiposReporte, tiposReporte[0]);
            if (tipo == -1) return;

            String ruta = pedirTexto("Ruta del archivo (ej: reporte.json):", "Exportar JSON");
            if (ruta == null || ruta.isBlank()) return;

            Object datos = (tipo == 0)
                ? reporteControlador.rankingBeneficioSocios()
                : prestamoControlador.listar();

            reporteControlador.exportarJson(datos, ruta);
            mostrarExito("Reporte exportado exitosamente en:\n" + ruta);

        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private static void exportarCsv() {
        try {
            String[] tiposReporte = {"Ranking de socios", "Lista de préstamos"};
            int tipo = JOptionPane.showOptionDialog(null,
                "¿Qué datos desea exportar?", "Exportar CSV",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, tiposReporte, tiposReporte[0]);
            if (tipo == -1) return;

            String ruta = pedirTexto("Ruta del archivo (ej: reporte.csv):", "Exportar CSV");
            if (ruta == null || ruta.isBlank()) return;

            List<?> datos = (tipo == 0)
                ? reporteControlador.rankingBeneficioSocios()
                : prestamoControlador.listar();

            reporteControlador.exportarCsv(datos, ruta);
            mostrarExito("Reporte CSV exportado exitosamente en:\n" + ruta);

        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UTILIDADES DE DIÁLOGO
    // ══════════════════════════════════════════════════════════════════════════

    private static String pedirTexto(String mensaje, String titulo) {
        return JOptionPane.showInputDialog(null, mensaje, titulo, JOptionPane.QUESTION_MESSAGE);
    }

    private static String pedirTextoConDefault(String mensaje, String valorActual, String titulo) {
        return (String) JOptionPane.showInputDialog(null,
            mensaje, titulo, JOptionPane.QUESTION_MESSAGE,
            null, null, valorActual);
    }

    private static void mostrarExito(String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, "✅ Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, "❌ Error", JOptionPane.ERROR_MESSAGE);
    }

    private static void mostrarInfo(String mensaje, String titulo) {
        JOptionPane.showMessageDialog(null, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    /** Muestra textos largos (tablas, listas) en un JScrollPane dentro de un JDialog. */
    private static void mostrarScrollable(String contenido, String titulo) {
        JTextArea area = new JTextArea(contenido);
        area.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new java.awt.Dimension(800, 400));
        JOptionPane.showMessageDialog(null, scroll, titulo, JOptionPane.PLAIN_MESSAGE);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FORMATEADORES
    // ══════════════════════════════════════════════════════════════════════════

    private static String formatearSocio(Socio s) {
        int numPrestamos = s.getPrestamos() == null ? 0 : s.getPrestamos().size();
        return String.format(
            "ID: %d\nNombre: %s\nCorreo: %s\nIngresos mensuales: $%.2f\nNivel de riesgo: %s\nPréstamos: %d",
            s.getId(), s.getNombre(), s.getCorreo(),
            s.getIngresosMensuales(), s.getNivelRiesgo(), numPrestamos);
    }

    private static String formatearSocioResumen(Socio s) {
        return String.format("[%d] %s | %s | Ingresos: $%.2f | Riesgo: %s",
            s.getId(), s.getNombre(), s.getCorreo(),
            s.getIngresosMensuales(), s.getNivelRiesgo());
    }

    private static String formatearPrestamo(Prestamo p) {
        int numPagos = p.getPagos() == null ? 0 : p.getPagos().size();
        return String.format(
            "Código: %d\nMonto: $%.2f\nTasa anual: %.2f%%\nPlazo: %d meses\nEstado: %s\nFecha inicio: %s\nPagos realizados: %d/%d",
            p.getCodigo(), p.getMontoSolicitado(), p.getTasaInteresAnual(),
            p.getPlazoMeses(), p.getEstado(), p.getFechaInicio(),
            numPagos, p.getPlazoMeses());
    }

    private static String formatearPrestamoResumen(Prestamo p) {
        return String.format("[#%d] $%.2f | %.1f%% anual | %d meses | Estado: %s | Inicio: %s",
            p.getCodigo(), p.getMontoSolicitado(), p.getTasaInteresAnual(),
            p.getPlazoMeses(), p.getEstado(), p.getFechaInicio());
    }
}