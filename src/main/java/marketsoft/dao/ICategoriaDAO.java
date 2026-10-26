package marketsoft.dao;

import marketsoft.model.Categoria;
import java.util.List;

/** Contrato de acceso a datos (el controller depende de esta interfaz, no de la clase concreta). */
public interface ICategoriaDAO {

    List<Categoria> listarTodas();

    boolean guardar(String nombre);

    boolean eliminar(int idCategoria);
}
