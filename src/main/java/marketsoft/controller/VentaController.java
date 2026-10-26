package marketsoft.controller;
import marketsoft.dao.VentaDAO;
import marketsoft.dao.IVentaDAO;
import marketsoft.model.DetalleVenta;
import marketsoft.model.Venta;
import java.sql.SQLException;
import java.util.List;

public class VentaController {

    private final IVentaDAO ventaDAO;

    public VentaController() {
        this(new VentaDAO());
    }

    public VentaController(IVentaDAO ventaDAO) {
        this.ventaDAO = ventaDAO;
    }

    public double calcularVuelto(double total, double montoRecibido) {
        return montoRecibido - total;
    }

    public boolean procesarVenta(List<DetalleVenta> carrito, double montoRecibido, String metodoPago,
            String dniCliente, String nombreCliente) throws SQLException {
        // Reglas de negocio en el controller (no solo en la vista):
        if (carrito == null || carrito.isEmpty()) {
            return false;           // no se registra una venta sin productos
        }
        if (dniCliente != null && !dniCliente.isBlank() && !dniCliente.trim().matches("\\d{8}")) {
            return false;           // DNI opcional, pero si viene debe tener 8 dígitos
        }
        double total = carrito.stream().mapToDouble(DetalleVenta::getSubtotal).sum();
        double vuelto = calcularVuelto(total, montoRecibido);
        if (vuelto < 0) {
            return false;
        }

        Venta venta = new Venta();
        venta.setTotal(total);
        venta.setMontoRecibido(montoRecibido);
        venta.setVuelto(vuelto);
        venta.setMetodoPago(metodoPago);
        venta.setDniCliente((dniCliente == null || dniCliente.isBlank()) ? null : dniCliente.trim());
        venta.setNombreCliente((nombreCliente == null || nombreCliente.isBlank()) ? null : nombreCliente.trim());
        carrito.forEach(venta::agregarDetalle);

        ventaDAO.registrarVenta(venta);
        return true;
    }

    public List<Venta> listarVentas() {
        return ventaDAO.listarVentas();
    }

    public boolean eliminarVenta(int idVenta) throws SQLException {
        return ventaDAO.eliminarVenta(idVenta);
    }

    public double porcentajeLimiteNRUS(double limiteMensual) {
        double ventasDelMes = ventaDAO.totalVentasDelMes();
        return limiteMensual <= 0 ? 0 : (ventasDelMes / limiteMensual) * 100;
    }

    public double totalVentasDelDia() {
        return ventaDAO.totalVentasDelDia();
    }

    public int conteoVentasDelDia() { return ventaDAO.contarVentasDelDia(); }

    public double ticketPromedioDia() { return ventaDAO.ticketPromedioDia(); }

    public double totalVentasDelMes() { return ventaDAO.totalVentasDelMes(); }

    // Opcion A: arqueo de caja + ganancia del dia
    public double totalEfectivoDia() { return ventaDAO.totalDiaPorMetodo("Efectivo"); }
    public double totalYapeDia() { return ventaDAO.totalDiaPorMetodo("Yape"); }
    public double totalPlinDia() { return ventaDAO.totalDiaPorMetodo("Plin"); }
    public double totalDigitalDia() { return ventaDAO.totalDigitalDia(); }
    public double gananciaEstimadaDia() { return ventaDAO.gananciaEstimadaDelDia(); }
}
