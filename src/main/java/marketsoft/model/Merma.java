package marketsoft.model;

public class Merma {
    private int idMerma;
    private int idProducto;
    private String nombreProducto;
    private int cantidad; // siempre en UNIDADES
    private String motivo;
    private String fecha; // YYYY-MM-DD HH:MM:SS
    private double costoUnitario; // para valorizar perdida
    private double perdidaTotal;

    public int getIdMerma() { return idMerma; }
    public void setIdMerma(int v) { this.idMerma = v; }
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int v) { this.idProducto = v; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String v) { this.nombreProducto = v; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int v) { this.cantidad = v; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String v) { this.motivo = v; }
    public String getFecha() { return fecha; }
    public void setFecha(String v) { this.fecha = v; }
    public double getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(double v) { this.costoUnitario = v; }
    public double getPerdidaTotal() { return perdidaTotal; }
    public void setPerdidaTotal(double v) { this.perdidaTotal = v; }
}
