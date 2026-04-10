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
class PrestamoServicioTest {

    @Mock private IPrestamoRepositorio prestamoRepo;
    @Mock private ISocioRepositorio socioRepo;

    private PrestamoServicio servicio;

    private Socio socioBase;

    @BeforeEach
    void setUp() {
        servicio = new PrestamoServicio(prestamoRepo, socioRepo);
        socioBase = new Socio("", "Kevin", "kevin@mail.com", 5000000, NivelRiesgo.BAJO);
        socioBase.setPrestamos(new ArrayList<>());
    }

    @Test
    void registrar_condicionesValidas_guardaPrestamo() {
        when(socioRepo.buscarPorId(socioBase.getId())).thenReturn(Optional.of(socioBase));

        Prestamo p = servicio.registrarPrestamo(
            socioBase.getId(), 1000000, 12, 12, LocalDate.now());

        verify(prestamoRepo, times(1)).guardar(any());
        assertNotNull(p);
        assertEquals(EstadoPrestamo.ACTIVO, p.getEstado());
    }

    @Test
    void registrar_socioInexistente_lanzaExcepcion() {
        when(socioRepo.buscarPorId(999)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
            servicio.registrarPrestamo(999, 1000000, 12, 12, LocalDate.now()));
    }

    @Test
    void registrar_montoNegativo_lanzaExcepcion() {
        when(socioRepo.buscarPorId(socioBase.getId())).thenReturn(Optional.of(socioBase));
        assertThrows(IllegalArgumentException.class, () ->
            servicio.registrarPrestamo(socioBase.getId(), -500000, 12, 12, LocalDate.now()));
    }

    @Test
    void registrar_cuotaSuperaCapacidad_lanzaExcepcion() {
        // Ingresos bajos para que la cuota supere el 30%
        Socio socioPobre = new Socio("", "Juan", "j@mail.com", 100000, NivelRiesgo.ALTO);
        socioPobre.setPrestamos(new ArrayList<>());
        when(socioRepo.buscarPorId(socioPobre.getId())).thenReturn(Optional.of(socioPobre));

        assertThrows(IllegalArgumentException.class, () ->
            servicio.registrarPrestamo(socioPobre.getId(), 10000000, 12, 12, LocalDate.now()));
    }

    @Test
    void registrar_masDeDosActivos_lanzaExcepcion() {
        // Agregar 2 préstamos activos al socio
        Prestamo p1 = new Prestamo(0, 12, 500000, 12, EstadoPrestamo.ACTIVO, LocalDate.now());
        Prestamo p2 = new Prestamo(0, 12, 500000, 12, EstadoPrestamo.ACTIVO, LocalDate.now());
        socioBase.setPrestamos(new ArrayList<>(List.of(p1, p2)));
        when(socioRepo.buscarPorId(socioBase.getId())).thenReturn(Optional.of(socioBase));

        assertThrows(IllegalArgumentException.class, () ->
            servicio.registrarPrestamo(socioBase.getId(), 500000, 12, 12, LocalDate.now()));
    }

    @Test
    void calcularCuotaMensual_valoresConocidos_retornaValorCorrecto() {
        // Préstamo de $1.000.000 al 12% anual a 12 meses
        // Cuota esperada ≈ $88.849
        Prestamo p = new Prestamo(0, 12, 1000000, 12, EstadoPrestamo.ACTIVO, LocalDate.now());
        double cuota = servicio.calcularCuotaMensual(p);
        assertEquals(88849.0, cuota, 1.0); // tolerancia de $1 por redondeo
    }

    @Test
    void calcularTablaAmortizacion_tieneNFilas() {
        Prestamo p = new Prestamo(0, 6, 1000000, 12, EstadoPrestamo.ACTIVO, LocalDate.now());
        p.setPagos(new ArrayList<>());
        List<CuotaAmortizacion> tabla = servicio.calcularTablaAmortizacion(p);
        assertEquals(6, tabla.size());
    }

    @Test
    void calcularTablaAmortizacion_saldoFinalUltimaCuotaEsCero() {
        Prestamo p = new Prestamo(0, 12, 1000000, 12, EstadoPrestamo.ACTIVO, LocalDate.now());
        p.setPagos(new ArrayList<>());
        List<CuotaAmortizacion> tabla = servicio.calcularTablaAmortizacion(p);
        assertEquals(0.0, tabla.get(tabla.size() - 1).getSalgoFinal(), 0.01);
    }

    @Test
    void cancelar_prestamoActivo_cambiaEstado() {
        Prestamo p = new Prestamo(0, 12, 1000000, 12, EstadoPrestamo.ACTIVO, LocalDate.now());
        when(prestamoRepo.buscarPorId(p.getCodigo())).thenReturn(Optional.of(p));

        servicio.cancelarPrestamo(p.getCodigo());

        assertEquals(EstadoPrestamo.CANCELADO, p.getEstado());
        verify(prestamoRepo).actualizar(p);
    }

    @Test
    void cancelar_prestamoYaCancelado_lanzaExcepcion() {
        Prestamo p = new Prestamo(0, 12, 1000000, 12, EstadoPrestamo.CANCELADO, LocalDate.now());
        when(prestamoRepo.buscarPorId(p.getCodigo())).thenReturn(Optional.of(p));

        assertThrows(IllegalStateException.class, () ->
            servicio.cancelarPrestamo(p.getCodigo()));
    }

    @Test
    void marcarMoroso_prestamoActivo_cambiaEstado() {
        Prestamo p = new Prestamo(0, 12, 1000000, 12, EstadoPrestamo.ACTIVO, LocalDate.now());
        when(prestamoRepo.buscarPorId(p.getCodigo())).thenReturn(Optional.of(p));

        servicio.marcarComoMoroso(p.getCodigo());

        assertEquals(EstadoPrestamo.MOROSO, p.getEstado());
    }

    @Test
    void reactivar_prestamoMoroso_vuelveActivo() {
        Prestamo p = new Prestamo(0, 12, 1000000, 12, EstadoPrestamo.MOROSO, LocalDate.now());
        when(prestamoRepo.buscarPorId(p.getCodigo())).thenReturn(Optional.of(p));

        servicio.reactivarPrestamo(p.getCodigo());

        assertEquals(EstadoPrestamo.ACTIVO, p.getEstado());
    }

    @Test
    void reactivar_prestamoNoMoroso_lanzaExcepcion() {
        Prestamo p = new Prestamo(0, 12, 1000000, 12, EstadoPrestamo.ACTIVO, LocalDate.now());
        when(prestamoRepo.buscarPorId(p.getCodigo())).thenReturn(Optional.of(p));

        assertThrows(IllegalStateException.class, () ->
            servicio.reactivarPrestamo(p.getCodigo()));
    }

    @Test
    void calcularSaldoPendiente_sinPagos_esMonto() {
        Prestamo p = new Prestamo(0, 12, 1000000, 12, EstadoPrestamo.ACTIVO, LocalDate.now());
        p.setPagos(new ArrayList<>());
        when(prestamoRepo.buscarPorId(p.getCodigo())).thenReturn(Optional.of(p));

        double saldo = servicio.calcularSaldoPendiente(p.getCodigo());

        assertEquals(1000000.0, saldo, 0.01);
    }
}
