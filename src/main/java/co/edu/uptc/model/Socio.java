package co.edu.uptc.model;

import java.util.ArrayList;

public class Socio {
    private static int contador = 100000;

    private int id;
    private String nombre;
    private String correo;
    private double ingresosMensuales;
    private NivelRiesgo nivelRiesgo;
    private ArrayList<Prestamo> prestamos = new ArrayList<>();
    
    public Socio(String id, String nombre, String correo, double ingresosMensuales, NivelRiesgo nivelRiesgo) {
        this.id = contador++;
        this.nombre = nombre;
        this.correo = correo;
        this.ingresosMensuales = ingresosMensuales;
        this.nivelRiesgo = nivelRiesgo;
    }

    public Socio() {
        this.id = contador++;
    }

    public static void setContador(int nuevoValor) {
        contador = nuevoValor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public double getIngresosMensuales() {
        return ingresosMensuales;
    }

    public void setIngresosMensuales(double ingresosMensuales) {
        this.ingresosMensuales = ingresosMensuales;
    }

    public NivelRiesgo getNivelRiesgo() {
        return nivelRiesgo;
    }

    public void setNivelRiesgo(NivelRiesgo nivelRiesgo) {
        this.nivelRiesgo = nivelRiesgo;
    }

    public ArrayList<Prestamo> getPrestamos() {
        return prestamos;
    }

    public void setPrestamos(ArrayList<Prestamo> prestamos) {
        this.prestamos = prestamos;
    }

    @Override
    public String toString() {
        return "Socio [id=" + id + ", nombre=" + nombre + ", correo=" + correo + ", ingresosMensuales="
                + ingresosMensuales + ", nivelRiesgo=" + nivelRiesgo + ", prestamos=" + prestamos + "]";
    }
}
