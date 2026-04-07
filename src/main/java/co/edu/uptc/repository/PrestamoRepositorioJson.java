package co.edu.uptc.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import co.edu.uptc.model.Prestamo;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class PrestamoRepositorioJson implements IPrestamoRepositorio {

    private static final Logger logger = Logger.getLogger(PrestamoRepositorioJson.class.getName());

    private final String archivoData;
    private final Gson gson;

    public PrestamoRepositorioJson(String archivoData) {
        this.archivoData = archivoData;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
                    @Override
                    public void write(JsonWriter out, LocalDate value) throws IOException {
                        if (value == null) { out.nullValue(); return; }
                        out.value(value.toString());
                    }
                    @Override
                    public LocalDate read(JsonReader in) throws IOException {
                        String s = in.nextString();
                        return s == null ? null : LocalDate.parse(s);
                    }
                })
                .setPrettyPrinting()
                .create();
        inicializarContador();
    }

    private void inicializarContador() {
        List<Prestamo> prestamos = leerArchivo();
        int maxCodigo = prestamos.stream()
                .mapToInt(Prestamo::getCodigo)
                .max()
                .orElse(0);
        Prestamo.setContador(maxCodigo + 1);

        // También inicializar el contador de Pagos si hay alguno
        int maxPagoId = 1000;
        for (Prestamo p : prestamos) {
            if (p.getPagos() != null) {
                for (co.edu.uptc.model.Pago pago : p.getPagos()) {
                    if (pago.getId() > maxPagoId) {
                        maxPagoId = pago.getId();
                    }
                }
            }
        }
        co.edu.uptc.model.Pago.setContador(maxPagoId + 1);
    }

    private List<Prestamo> leerArchivo() {
        File archivo = new File(archivoData);
        if (!archivo.exists()) {
            logger.warning("Archivo no encontrado: " + archivoData);
            return new ArrayList<>();
        }
        try (Reader reader = new FileReader(archivo)) {
            Type tipoLista = new TypeToken<List<Prestamo>>() {}.getType();
            List<Prestamo> prestamos = gson.fromJson(reader, tipoLista);
            return prestamos != null ? prestamos : new ArrayList<>();
        } catch (IOException e) {
            logger.severe("Error al leer el archivo: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void guardarEnArchivo(List<Prestamo> prestamos) {
        // Crear directorios si no existen
        File archivo = new File(archivoData);
        if (archivo.getParentFile() != null) {
            archivo.getParentFile().mkdirs();
        }
        try (Writer writer = new FileWriter(archivo)) {
            gson.toJson(prestamos, writer);
            logger.info("Archivo guardado correctamente: " + archivoData);
        } catch (IOException e) {
            logger.severe("Error al escribir el archivo: " + e.getMessage());
        }
    }

    @Override
    public void guardar(Prestamo prestamo) {
        List<Prestamo> prestamos = leerArchivo();
        prestamos.add(prestamo);
        guardarEnArchivo(prestamos);
        logger.info("Préstamo guardado: " + prestamo.getCodigo());
    }

    @Override
    public Optional<Prestamo> buscarPorId(int id) {
        return leerArchivo().stream()
                .filter(p -> p.getCodigo() == id)
                .findFirst();
    }

    @Override
    public List<Prestamo> listarTodos() {
        return leerArchivo();
    }

    @Override
    public void actualizar(Prestamo prestamoActualizado) {
        List<Prestamo> prestamos = leerArchivo();
        for (int i = 0; i < prestamos.size(); i++) {
            if (prestamos.get(i).getCodigo() == prestamoActualizado.getCodigo()) {
                prestamos.set(i, prestamoActualizado);
                break;
            }
        }
        guardarEnArchivo(prestamos);
        logger.info("Préstamo actualizado: " + prestamoActualizado.getCodigo());
    }

    @Override
    public void eliminar(int id) {
        List<Prestamo> prestamos = leerArchivo();
        prestamos.removeIf(p -> p.getCodigo() == id);
        guardarEnArchivo(prestamos);
        logger.info("Préstamo eliminado: " + id);
    }
}
