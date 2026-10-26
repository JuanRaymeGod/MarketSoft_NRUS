package marketsoft.controller;
import marketsoft.dao.CategoriaDAO;
import marketsoft.dao.ICategoriaDAO;
import marketsoft.model.Categoria;
import java.util.List;

public class CategoriaController {
    private final ICategoriaDAO categoriaDAO;

    public CategoriaController() {
        this(new CategoriaDAO());
    }

    public CategoriaController(ICategoriaDAO categoriaDAO) {
        this.categoriaDAO = categoriaDAO;
    }

    public List<Categoria> listarCategorias() { return categoriaDAO.listarTodas(); }

    public boolean guardarCategoria(String nombre) {
        if (nombre == null || nombre.isBlank()) return false;
        return categoriaDAO.guardar(nombre.trim());
    }

    public boolean eliminarCategoria(int id) { return categoriaDAO.eliminar(id); }
}
