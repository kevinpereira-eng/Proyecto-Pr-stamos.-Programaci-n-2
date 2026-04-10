package co.edu.uptc.service;

import co.edu.uptc.model.NivelRiesgo;
import co.edu.uptc.model.Socio;
import co.edu.uptc.repository.ISocioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocioServicioTest {

    @Mock
    private ISocioRepositorio socioRepo;

    private SocioServicio servicio;

    @BeforeEach
    void setUp() {
        servicio = new SocioServicio(socioRepo);
    }

    @Test
    void registrar_socioValido_guardaCorrectamente() {
        Socio s = new Socio("", "Kevin Pereira", "kevin@mail.com", 3000000, NivelRiesgo.BAJO);
        servicio.registrarSocio(s);
        verify(socioRepo, times(1)).guardar(s);
    }

    @Test
    void registrar_nombreVacio_lanzaExcepcion() {
        Socio s = new Socio("", "", "kevin@mail.com", 3000000, NivelRiesgo.BAJO);
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarSocio(s));
    }

    @Test
    void registrar_correoVacio_lanzaExcepcion() {
        Socio s = new Socio("", "Kevin", "", 3000000, NivelRiesgo.BAJO);
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarSocio(s));
    }

    @Test
    void registrar_ingresosCero_lanzaExcepcion() {
        Socio s = new Socio("", "Kevin", "kevin@mail.com", 0, NivelRiesgo.BAJO);
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarSocio(s));
    }

    @Test
    void buscarPorId_socioExistente_retornaSocio() {
        Socio s = new Socio("", "Kevin", "kevin@mail.com", 3000000, NivelRiesgo.BAJO);
        when(socioRepo.buscarPorId(s.getId())).thenReturn(Optional.of(s));

        Optional<Socio> resultado = servicio.buscarPorId(s.getId());

        assertTrue(resultado.isPresent());
        assertEquals("Kevin", resultado.get().getNombre());
    }

    @Test
    void buscarPorId_socioInexistente_retornaVacio() {
        when(socioRepo.buscarPorId(999)).thenReturn(Optional.empty());
        assertTrue(servicio.buscarPorId(999).isEmpty());
    }

    @Test
    void eliminar_socioExistente_eliminaCorrectamente() {
        Socio s = new Socio("", "Kevin", "kevin@mail.com", 3000000, NivelRiesgo.BAJO);
        when(socioRepo.buscarPorId(s.getId())).thenReturn(Optional.of(s));

        servicio.eliminarSocio(s.getId());

        verify(socioRepo, times(1)).eliminar(s.getId());
    }

    @Test
    void eliminar_socioInexistente_lanzaExcepcion() {
        when(socioRepo.buscarPorId(999)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> servicio.eliminarSocio(999));
    }

    @Test
    void listar_retornaTodosLosSocios() {
        Socio s1 = new Socio("", "Kevin", "k@mail.com", 2000000, NivelRiesgo.BAJO);
        Socio s2 = new Socio("", "Laura", "l@mail.com", 3000000, NivelRiesgo.MEDIO);
        when(socioRepo.listarTodos()).thenReturn(List.of(s1, s2));

        List<Socio> resultado = servicio.listarSocios();

        assertEquals(2, resultado.size());
    }
}
