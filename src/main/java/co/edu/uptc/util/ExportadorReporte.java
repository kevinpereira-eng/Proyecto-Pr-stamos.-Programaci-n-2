package co.edu.uptc.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.logging.Logger;

public class ExportadorReporte {

    private static final Logger logger = Logger.getLogger(ExportadorReporte.class.getName());

    private final Gson gson;

    public ExportadorReporte() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void exportarJson(Object datos, String ruta) {
        logger.info("Exportando reporte a JSON en: " + ruta);

        try (Writer writer = new FileWriter(ruta)) {
            gson.toJson(datos, writer);
            logger.info("Reporte JSON exportado exitosamente en: " + ruta);
        } catch (IOException e) {
            logger.severe("Error al exportar reporte JSON: " + e.getMessage());
            throw new RuntimeException("No se pudo exportar el archivo JSON: " + e.getMessage());
        }
    }

    public void exportarCsv(List<?> lista, String ruta) {
        logger.info("Exportando reporte a CSV en: " + ruta);

        if (lista == null || lista.isEmpty()) {
            logger.warning("La lista está vacía, no se generará el archivo CSV");
            return;
        }

        try (FileWriter writer = new FileWriter(ruta)) {

            // Obtener los campos del objeto mediante reflexión
            var campos = lista.get(0).getClass().getDeclaredFields();

            // Encabezado
            StringBuilder encabezados = new StringBuilder();
            for (int i = 0; i < campos.length; i++) {
                encabezados.append(campos[i].getName());
                if (i < campos.length - 1) encabezados.append(",");
            }
            writer.write(encabezados + "\n");

            // Escribir filas
            for (Object objeto : lista) {
                StringBuilder fila = new StringBuilder();
                for (int i = 0; i < campos.length; i++) {
                    campos[i].setAccessible(true);
                    Object valor = campos[i].get(objeto);
                    fila.append(valor != null ? valor.toString() : "");
                    if (i < campos.length - 1) fila.append(",");
                }
                writer.write(fila + "\n");
            }

            logger.info("Reporte CSV exportado exitosamente en: " + ruta);

        } catch (IOException | IllegalAccessException e) {
            logger.severe("Error al exportar reporte CSV: " + e.getMessage());
            throw new RuntimeException("No se pudo exportar el archivo CSV: " + e.getMessage());
        }
    }
}