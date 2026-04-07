package co.edu.uptc.model;

public class RankingSocio {


    private int idSocio;
    private String nombreSocio;
    private double montoTotalFinanciado;
    private int cantidadPrestamos;

    public RankingSocio(int idSocio, String nombreSocio, double montoTotalFinanciado, int cantidadPrestamos) {
        this.idSocio = idSocio;
        this.nombreSocio = nombreSocio;
        this.montoTotalFinanciado = montoTotalFinanciado;
        this.cantidadPrestamos = cantidadPrestamos;
    }

    public String getIdSocio() {
        return String.valueOf(idSocio);
    }

    public double getMontoTotalFinanciado() {
        return montoTotalFinanciado;
    }

    public String getNombreSocio() {
        return nombreSocio;
    }

    public void setNombreSocio(String nombreSocio) {
        this.nombreSocio = nombreSocio;
    }

    public void setIdSocio(int idSocio) {
        this.idSocio = idSocio;
    }

    public void setMontoTotalFinanciado(double montoTotalFinanciado) {
        this.montoTotalFinanciado = montoTotalFinanciado;
    }

    public int getCantidadPrestamos() {
        return cantidadPrestamos;
    }

    public void setCantidadPrestamos(int cantidadPrestamos) {
        this.cantidadPrestamos = cantidadPrestamos;
    }

    @Override
    public String toString() {
        return "RankingSocio{" +
                "idSocio=" + idSocio +
                ", nombreSocio='" + nombreSocio + '\'' +
                ", montoTotalFinanciado=" + montoTotalFinanciado +
                ", cantidadPrestamos=" + cantidadPrestamos +
                '}';
    }
}