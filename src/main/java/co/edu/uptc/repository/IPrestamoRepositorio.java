package co.edu.uptc.repository;

import co.edu.uptc.model.Prestamo;

import java.util.List;
import java.util.Optional;

public interface IPrestamoRepositorio {
    void guardar(Prestamo p);
    Optional<Prestamo> buscarPorId(int id);
    List<Prestamo> listarTodos();
    void actualizar(Prestamo p);
    void eliminar(int id);
}
