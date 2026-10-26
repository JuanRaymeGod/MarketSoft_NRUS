package marketsoft.model;

// Fila para la exportación a CSV: una fila POR VENTA (no por producto),
// porque el cliente/DNI solo existen a nivel de venta completa.
public class FilaVenta {

    public int idVenta;
    public String fecha; // tal como viene de SQLite ("YYYY-MM-DD HH:MM:SS")
    public String cliente;
    public String dni;
    public String metodoPago;
    public double ventaTotal, costoTotal, gananciaNeta;
}
