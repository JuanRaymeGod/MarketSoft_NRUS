package marketsoft.dao;

import marketsoft.model.Producto;
import marketsoft.model.ResultadoEliminacion;
import java.util.List;

/** Contrato de acceso a datos (el controller depende de esta interfaz, no de la clase concreta). */
public interface IProductoDAO {

    boolean guardar(Producto p);

    boolean actualizar(Producto p);

    List<Producto> listarTodos();

    List<Producto> buscarPorNombre(String texto);

    Producto buscarPorId(int idProducto);

    List<Producto> listarStockBajo();

    List<Producto> listarProximosAVencer(int diasLimite);

    boolean aumentarStock(int idProducto, int cantidad);

    boolean disminuirStock(int idProducto, int cantidad);

    ResultadoEliminacion eliminar(int idProducto);
}
