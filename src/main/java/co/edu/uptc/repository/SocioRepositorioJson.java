package co.edu.uptc.repository;

import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;

import co.edu.uptc.model.Socio;

public class SocioRepositorioJson implements ISocioRepositorio {
    private String archivoRuta;
    private Gson gson;
    @Override
    public void guardar(Socio s) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'guardar'");
    }
    @Override
    public Optional<Socio> buscarPorId(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buscarPorId'");
    }
    @Override
    public List<Socio> listarTodos() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listarTodos'");
    }
    @Override
    public void actualizar(Socio s) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'actualizar'");
    }
    @Override
    public void eliminar(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'eliminar'");
    }


}
