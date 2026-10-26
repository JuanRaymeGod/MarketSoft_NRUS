package marketsoft.controller;
import marketsoft.dao.ProductoDAO;
import marketsoft.dao.IProductoDAO;
import marketsoft.model.Producto;
import marketsoft.model.ResultadoEliminacion;
import java.util.List;

public class ProductoController {

    private final IProductoDAO productoDAO;

    public ProductoController() {
        this(new ProductoDAO());
    }

    public ProductoController(IProductoDAO productoDAO) {
        this.productoDAO = productoDAO;
    }

    public List<Producto> listarProductos() {
        return productoDAO.listarTodos();
    }

    public List<Producto> buscar(String texto) {
        return productoDAO.buscarPorNombre(texto);
    }

    public Producto obtenerPorId(int id) {
        return productoDAO.buscarPorId(id);
    }

    public List<Producto> alertasStockBajo() {
        return productoDAO.listarStockBajo();
    }

    public List<Producto> alertasPorVencer() {
        return productoDAO.listarProximosAVencer(5);
    }

    public boolean guardarProducto(Producto p) {
        if (p.getNombre() == null || p.getNombre().isBlank()) {
            return false;
        }
        if (p.isVieneEnCaja() && p.getUnidadesPorCaja() > 0) {
            double precioUnitarioCalculado = p.getPrecioCompraCaja() / p.getUnidadesPorCaja();
            p.setPrecioCompraUnitario(precioUnitarioCalculado);
        }
        return p.getIdProducto() == 0 ? productoDAO.guardar(p) : productoDAO.actualizar(p);
    }

    public boolean registrarIngresoStock(int idProducto, int cantidad) {
        if (cantidad <= 0) {
            return false;
        }
        return productoDAO.aumentarStock(idProducto, cantidad);
    }

    public boolean registrarRetiroStock(int idProducto, int cantidad) {
        if (cantidad <= 0) {
            return false;
        }
        return productoDAO.disminuirStock(idProducto, cantidad);
    }

    // Botón "💲 Actualizar Precio": respeta el modo de compra actual del producto
    // (si viene en caja, nuevoCostoCapturado es el precio de LA CAJA; si no, es por unidad).
    public boolean actualizarPrecios(int idProducto, double nuevoCostoCapturado, double nuevoPrecioVenta) {
        if (nuevoPrecioVenta < 0 || nuevoCostoCapturado < 0) return false;
        Producto p = productoDAO.buscarPorId(idProducto);
        if (p == null) return false;

        if (p.isVieneEnCaja() && p.getUnidadesPorCaja() > 0) {
            p.setPrecioCompraCaja(nuevoCostoCapturado);
            p.setPrecioCompraUnitario(nuevoCostoCapturado / p.getUnidadesPorCaja());
        } else {
            p.setPrecioCompraUnitario(nuevoCostoCapturado);
        }
        p.setPrecioVentaUnitario(nuevoPrecioVenta);
        return productoDAO.actualizar(p);
    }

    // Botón "🗑 Retirar Producto": borra si no tiene historial; si lo tiene, lo oculta (ver ProductoDAO.eliminar).
    public ResultadoEliminacion eliminarProducto(int idProducto) {
        return productoDAO.eliminar(idProducto);
    }
}
