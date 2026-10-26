package marketsoft.model;

public class DetalleVenta {
    private int idProducto;
    private String nombreProducto; // solo para mostrar en el carrito
    private int cantidad;
    private double precioUnitario;
    private double costoUnitarioCompra; // foto histórica del costo al momento de la venta
    private double subtotal;

    public DetalleVenta() {
    }

    public DetalleVenta(int idProducto, String nombreProducto, int cantidad, double precioUnitario,
                         double costoUnitarioCompra) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.costoUnitarioCompra = costoUnitarioCompra;
        this.subtotal = cantidad * precioUnitario;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        this.subtotal = cantidad * precioUnitario;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double getCostoUnitarioCompra() {
        return costoUnitarioCompra;
    }

    public double getSubtotal() {
        return subtotal;
    }
}