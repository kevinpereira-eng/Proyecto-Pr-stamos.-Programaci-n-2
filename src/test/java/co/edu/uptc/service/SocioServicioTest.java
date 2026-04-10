package co.edu.uptc.service;

import co.edu.uptc.model.*;
import co.edu.uptc.repository.IPrestamoRepositorio;
import co.edu.uptc.repository.ISocioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServicioTest {

    @Mock private IPrestamoRepositorio prestamoRepo;
    @Mock private ISocioRepositorio socioRepo;
    @Mock private PrestamoServicio prestamoServicio;

    private PagoServicio servicio;
    private Prestamo prestamoActivo;

    @BeforeEach
    void setUp() {
        servicio = new PagoServicio(prestamoRepo, socioRepo, prestamoServicio);

        prestamoActivo = new Prestamo(0, 3, 1000000, 12, EstadoPrestamo.ACTIVO, LocalDate.now());
        prestamoActivo.setPagos(new ArrayList<>());
    }

    @Test
    void registrarPago_primeraCuota_creaElPago() {
        // Simular tabla de amortización con 3 cuotas
        List<CuotaAmortizacion> tabla = List.of(
            new CuotaAmortizacion(1, 340002, 330002, 10000, 1000000, 670000, false, LocalDate.now().plusMonths(1)),
            new CuotaAmortizacion(2, 340002, 333302, 6700,  670000,  336698, false, LocalDate.now().plusMonths(2)),
            new CuotaAmortizacion(3, 340002, 336698, 3365,  336698,  0,      false, LocalDate.now().plusMonths(3))
        );

        when(prestamoRepo.buscarPorId(prestamoActivo.getCodigo()))
            .thenReturn(Optional.of(prestamoActivo));
        when(prestamoServicio.calcularTablaAmortizacion(prestamoActivo))
            .thenReturn(tabla);
        when(socioRepo.listarTodos()).thenReturn(new ArrayList<>());

        Pago pago = servicio.registrarPago(prestamoActivo.getCodigo(), LocalDate.now());

        assertNotNull(pago);
        assertEquals(340002, pago.getMontoPagado(), 1.0);
        assertEquals(1, prestamoActivo.getPagos().size());
    }

    @Test
    void registrarPago_prestamoInexistente_lanzaExcepcion() {
        when(prestamoRepo.buscarPorId(999)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
            servicio.registrarPago(999, LocalDate.now()));
    }

    @Test
    void registrarPago_prestamoCancelado_lanzaExcepcion() {
        prestamoActivo.setEstado(EstadoPrestamo.CANCELADO);
        when(prestamoRepo.buscarPorId(prestamoActivo.getCodigo()))
            .thenReturn(Optional.of(prestamoActivo));

        assertThrows(IllegalStateException.class, () ->
            servicio.registrarPago(prestamoActivo.getCodigo(), LocalDate.now()));
    }

    @Test
    void registrarPagoAnticipado_montoValido_creaElPago() {
        when(prestamoRepo.buscarPorId(prestamoActivo.getCodigo()))
            .thenReturn(Optional.of(prestamoActivo));
        when(prestamoServicio.calcularSaldoPendiente(prestamoActivo.getCodigo()))
            .thenReturn(1000000.0);
        when(socioRepo.listarTodos()).thenReturn(new ArrayList<>());

        Pago pago = servicio.registrarPagoAnticipado(
            prestamoActivo.getCodigo(), 500000, LocalDate.now());

        assertNotNull(pago);
        assertEquals(500000, pago.getMontoPagado(), 1.0);
    }

    @Test
    void registrarPagoAnticipado_montoNegativo_lanzaExcepcion() {
        when(prestamoRepo.buscarPorId(prestamoActivo.getCodigo()))
            .thenReturn(Optional.of(prestamoActivo));

        assertThrows(IllegalArgumentException.class, () ->
            servicio.registrarPagoAnticipado(prestamoActivo.getCodigo(), -100, LocalDate.now()));
    }

    @Test
    void listarPagos_sinPagos_retornaListaVacia() {
        when(prestamoRepo.buscarPorId(prestamoActivo.getCodigo()))
            .thenReturn(Optional.of(prestamoActivo));

        List<Pago> pagos = servicio.listarPagosPorPrestamo(prestamoActivo.getCodigo());

        assertTrue(pagos.isEmpty());
    }
}
