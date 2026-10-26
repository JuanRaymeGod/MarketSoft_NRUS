package marketsoft.fakes;

import marketsoft.dao.IProductoDAO;
import marketsoft.model.Producto;
import marketsoft.model.ResultadoEliminacion;
import java.util.ArrayList;
import java.util.List;

public class FakeProductoDAO implements IProductoDAO {

    public final List<Producto> guardados = new ArrayList<>();
    public final List<Producto> actualizados = new ArrayList<>();
    public Producto productoPorId = null;          // lo que devuelve buscarPorId
    public int diasLimiteRecibido = -1;
    public int llamadasAumentar = 0, llamadasDisminuir = 0;
    public int ultimoIdStock, ultimaCantidadStock;
    public ResultadoEliminacion resultadoEliminar = ResultadoEliminacion.ELIMINADO;

    @Override public boolean guardar(Producto p) { guardados.add(p); return true; }
    @Override public boolean actualizar(Producto p) { actualizados.add(p); return true; }
    @Override public List<Producto> listarTodos() { return new ArrayList<>(); }
    @Override public List<Producto> buscarPorNombre(String texto) { return new ArrayList<>(); }
    @Override public Producto buscarPorId(int idProducto) { return productoPorId; }
    @Override public List<Producto> listarStockBajo() { return new ArrayList<>(); }

    @Override
    public List<Producto> listarProximosAVencer(int diasLimite) {
        diasLimiteRecibido = diasLimite;
        return new ArrayList<>();
    }

    @Override
    public boolean aumentarStock(int idProducto, int cantidad) {
        llamadasAumentar++; ultimoIdStock = idProducto; ultimaCantidadStock = cantidad;
        return true;
    }

    @Override
    public boolean disminuirStock(int idProducto, int cantidad) {
        llamadasDisminuir++; ultimoIdStock = idProducto; ultimaCantidadStock = cantidad;
        return true;
    }

    @Override
    public ResultadoEliminacion eliminar(int idProducto) { return resultadoEliminar; }
}
