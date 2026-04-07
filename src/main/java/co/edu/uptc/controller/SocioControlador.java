package co.edu.uptc.controller;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import co.edu.uptc.model.Socio;
import co.edu.uptc.service.SocioServicio;

public class SocioControlador {

    private static final Logger logger = Logger.getLogger(SocioControlador.class.getName());

    private final SocioServicio socioServicio;

    public SocioControlador(SocioServicio socioServicio) {
        this.socioServicio = socioServicio;
    }

    // ─── Métodos del UML ────────────────────────────────────────────────────

    public void registrar(Socio s) {
        try {
            logger.info("Controlador: registrando socio");
            socioServicio.registrarSocio(s);
            logger.info("Controlador: socio registrado exitosamente");
        } catch (IllegalArgumentException e) {
            logger.warning("Controlador: error al registrar socio - " + e.getMessage());
            throw e;
        }
    }

    public Optional<Socio> consultar(int id) {
        try {
            logger.info("Controlador: consultando socio con ID: " + id);
            return socioServicio.buscarPorId(id);
        } catch (Exception e) {
            logger.warning("Controlador: error al consultar socio - " + e.getMessage());
            throw e;
        }
    }

    public void modificar(Socio s) {
        try {
            logger.info("Controlador: modificando socio con ID: " + s.getId());
            socioServicio.modificarSocio(s);
            logger.info("Controlador: socio modificado exitosamente");
        } catch (IllegalArgumentException e) {
            logger.warning("Controlador: error al modificar socio - " + e.getMessage());
            throw e;
        }
    }

    public void eliminar(int id) {
        try {
            logger.info("Controlador: eliminando socio con ID: " + id);
            socioServicio.eliminarSocio(id);
            logger.info("Controlador: socio eliminado exitosamente");
        } catch (RuntimeException e) {
            logger.warning("Controlador: error al eliminar socio - " + e.getMessage());
            throw e;
        }
    }

    public List<Socio> listar() {
        try {
            logger.info("Controlador: listando todos los socios");
            return socioServicio.listarSocios();
        } catch (Exception e) {
            logger.warning("Controlador: error al listar socios - " + e.getMessage());
            throw e;
        }
    }
}