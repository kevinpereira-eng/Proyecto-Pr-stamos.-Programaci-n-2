package co.edu.uptc.service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import co.edu.uptc.model.Socio;
import co.edu.uptc.repository.ISocioRepositorio;

public class SocioServicio {

    private static final Logger logger = Logger.getLogger(SocioServicio.class.getName());

    private final ISocioRepositorio socioRepo;

    public SocioServicio(ISocioRepositorio socioRepo) {
        this.socioRepo = socioRepo;
    }

    public void registrarSocio(Socio s) {
        logger.info("Registrando socio: " + s.getId());
        validarSocio(s);
        socioRepo.guardar(s);
        logger.info("Socio registrado exitosamente: " + s.getId());
    }

    public Optional<Socio> buscarPorId(int id) {
        logger.info("Buscando socio con ID: " + id);
        return socioRepo.buscarPorId(id);
    }

    public void modificarSocio(Socio s) {
        logger.info("Modificando socio: " + s.getId());

        socioRepo.buscarPorId(s.getId())
                .orElseThrow(() -> new RuntimeException("Socio no encontrado: " + s.getId()));

        validarSocio(s);
        socioRepo.actualizar(s);
        logger.info("Socio modificado exitosamente: " + s.getId());
    }

    public List<Socio> listarSocios() {
        logger.info("Listando todos los socios");
        return socioRepo.listarTodos();
    }

    public void eliminarSocio(int id) {
        logger.info("Eliminando socio con ID: " + id);

        socioRepo.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Socio no encontrado: " + id));

        socioRepo.eliminar(id);
        logger.info("Socio eliminado exitosamente: " + id);
    }

    public void validarSocio(Socio s) {
        logger.info("Validando socio: " + s.getId());

        if (s.getNombre() == null || s.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del socio no puede estar vacío");
        }

        if (s.getCorreo() == null || s.getCorreo().isBlank()) {
            throw new IllegalArgumentException("El correo electrónico no puede estar vacío");
        }

        if (s.getIngresosMensuales() <= 0) {
            throw new IllegalArgumentException("Los ingresos mensuales deben ser mayores a 0");
        }

        // Validación del PDF: no más de 2 préstamos activos
        if (!puedeAccederPrestamo(s)) {
            throw new IllegalArgumentException("El socio ya tiene 2 préstamos activos");
        }

        logger.info("Socio validado correctamente: " + s.getId());
    }

    public boolean tieneCondicionesDisponibles(Socio s) {
        return s.getIngresosMensuales() > 0 && s.getPrestamos() != null;
    }

    public boolean puedeAccederPrestamo(Socio s) {
        //no permitir más de 2 préstamos activos
        if (s.getPrestamos() == null) return true;

        long prestamosActivos = s.getPrestamos().stream()
                .filter(p -> p.getEstado().name().equals("ACTIVO"))
                .count();

        return prestamosActivos < 2;
    }

    public void agregarPrestamo(Socio s) {
        logger.info("Agregando préstamo al socio: " + s.getId());

        if (!puedeAccederPrestamo(s)) {
            throw new IllegalArgumentException("El socio no puede tener más de 2 préstamos activos");
        }

        socioRepo.actualizar(s);
        logger.info("Préstamo agregado al socio: " + s.getId());
    }

    public void eliminarPrestamo(Socio s) {
        logger.info("Eliminando préstamo del socio: " + s.getId());
        socioRepo.actualizar(s);
        logger.info("Préstamo eliminado del socio: " + s.getId());
    }
}