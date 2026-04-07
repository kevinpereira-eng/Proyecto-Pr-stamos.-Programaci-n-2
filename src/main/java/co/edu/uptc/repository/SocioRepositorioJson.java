package co.edu.uptc.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import co.edu.uptc.model.Socio;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class SocioRepositorioJson implements ISocioRepositorio {

    private static final Logger logger = Logger.getLogger(SocioRepositorioJson.class.getName());

    private final String archivoData;
    private final Gson gson;

    public SocioRepositorioJson(String archivoData) {
        this.archivoData = archivoData;
        this.gson = new com.google.gson.GsonBuilder()
                .registerTypeAdapter(java.time.LocalDate.class, new com.google.gson.TypeAdapter<java.time.LocalDate>() {
                    @Override
                    public void write(com.google.gson.stream.JsonWriter out, java.time.LocalDate value) throws java.io.IOException {
                        if (value == null) { out.nullValue(); return; }
                        out.value(value.toString());
                    }
                    @Override
                    public java.time.LocalDate read(com.google.gson.stream.JsonReader in) throws java.io.IOException {
                        String s = in.nextString();
                        return s == null ? null : java.time.LocalDate.parse(s);
                    }
                })
                .setPrettyPrinting()
                .create();
        inicializarContador();
    }

    private void inicializarContador() {
        List<Socio> socios = leerArchivo();
        int maxId = socios.stream()
                .mapToInt(Socio::getId)
                .max()
                .orElse(100000);
        Socio.setContador(maxId + 1);
    }

    // leer y escribir el Json:

    private List<Socio> leerArchivo() {
        File archivo = new File(archivoData);

        if (!archivo.exists()) {
            logger.warning("Archivo no encontrado: " + archivoData);
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(archivo)) {
            Type tipoLista = new TypeToken<List<Socio>>() {}.getType();
            List<Socio> socios = gson.fromJson(reader, tipoLista);
            return socios != null ? socios : new ArrayList<>();
        } catch (IOException e) {
            logger.severe("Error al leer el archivo: " + e.getMessage());
            return new ArrayList<>();
        } catch (com.google.gson.JsonSyntaxException e) {
            logger.severe("Error de sintaxis en el archivo JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void guardarEnArchivo(List<Socio> socios) {
        File archivo = new File(archivoData);
        if (archivo.getParentFile() != null) {
            archivo.getParentFile().mkdirs();
        }
        try (Writer writer = new FileWriter(archivo)) {
            gson.toJson(socios, writer);
            logger.info("Archivo guardado correctamente: " + archivoData);
        } catch (IOException e) {
            logger.severe("Error al escribir el archivo: " + e.getMessage());
            throw new RuntimeException("No se pudo guardar la información en el archivo: " + e.getMessage());
        }
    }

    @Override
    public void guardar(Socio socio) {
        List<Socio> socios = leerArchivo();
        socios.add(socio);
        guardarEnArchivo(socios);
        logger.info("Socio guardado: " + socio.getId());
    }

    @Override
    public Optional<Socio> buscarPorId(int id) {
        return leerArchivo().stream()
                .filter(s -> s.getId()==id)
                .findFirst();
    }

    @Override
    public List<Socio> listarTodos() {
        return leerArchivo();
    }

    @Override
    public void actualizar(Socio socioActualizado) {
        List<Socio> socios = leerArchivo();

        for (int i = 0; i < socios.size(); i++) {
            if (socios.get(i).getId()==socioActualizado.getId()) {
                socios.set(i, socioActualizado);
                break;
            }
        }

        guardarEnArchivo(socios);
        logger.info("Socio actualizado: " + socioActualizado.getId());
    }

    @Override
    public void eliminar(int id) {
        List<Socio> socios = leerArchivo();
        socios.removeIf(s -> s.getId()==id);
        guardarEnArchivo(socios);
        logger.info("Socio eliminado: " + id);
    }
}