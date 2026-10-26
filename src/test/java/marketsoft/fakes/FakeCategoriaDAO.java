package marketsoft.fakes;

import marketsoft.dao.ICategoriaDAO;
import marketsoft.model.Categoria;
import java.util.ArrayList;
import java.util.List;

public class FakeCategoriaDAO implements ICategoriaDAO {

    public List<Categoria> lista = new ArrayList<>();
    public int llamadasGuardar = 0;
    public String ultimoNombre;
    public int ultimoIdEliminado = -1;

    @Override
    public List<Categoria> listarTodas() {
        return lista;
    }

    @Override
    public boolean guardar(String nombre) {
        llamadasGuardar++;
        ultimoNombre = nombre;
        return true;
    }

    @Override
    public boolean eliminar(int idCategoria) {
        ultimoIdEliminado = idCategoria;
        return true;
    }
}
