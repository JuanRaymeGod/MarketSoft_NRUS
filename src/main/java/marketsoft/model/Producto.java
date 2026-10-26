package marketsoft.model;
import java.time.LocalDate;

public class Producto {
    private int idProducto;
    private String codigoBarras;
    private String nombre;
    private int idCategoria;
    private String nombreCategoria; // solo para mostrar en tablas
    private double precioCompraUnitario;
    private double precioVentaUnitario;
    private boolean vieneEnCaja;
    private int unidadesPorCaja;
    private double precioCompraCaja;
    private int stockUnidades;
    private int stockMinimo;
    private LocalDate fechaVencimiento;

    public Producto() {
    }

    // Getters y setters de todos los campos
    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public double getPrecioCompraUnitario() {
        return precioCompraUnitario;
    }

    public void setPrecioCompraUnitario(double v) {
        this.precioCompraUnitario = v;
    }

    public double getPrecioVentaUnitario() {
        return precioVentaUnitario;
    }

    public void setPrecioVentaUnitario(double v) {
        this.precioVentaUnitario = v;
    }

    public boolean isVieneEnCaja() {
        return vieneEnCaja;
    }

    public void setVieneEnCaja(boolean v) {
        this.vieneEnCaja = v;
    }

    public int getUnidadesPorCaja() {
        return unidadesPorCaja;
    }

    public void setUnidadesPorCaja(int v) {
        this.unidadesPorCaja = v;
    }

    public double getPrecioCompraCaja() {
        return precioCompraCaja;
    }

    public void setPrecioCompraCaja(double v) {
        this.precioCompraCaja = v;
    }

    public int getStockUnidades() {
        return stockUnidades;
    }

    public void setStockUnidades(int v) {
        this.stockUnidades = v;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int v) {
        this.stockMinimo = v;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate v) {
        this.fechaVencimiento = v;
    }
}
