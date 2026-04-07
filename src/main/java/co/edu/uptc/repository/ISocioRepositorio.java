package co.edu.uptc.repository;

import java.util.List;
import java.util.Optional;

import co.edu.uptc.model.Socio;

public interface ISocioRepositorio {
    void guardar(Socio s);    
    Optional<Socio> buscarPorId(int id); 
    List<Socio> listarTodos();
    void actualizar(Socio s);
    void eliminar(int id);
}
