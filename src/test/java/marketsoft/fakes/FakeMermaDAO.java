package marketsoft.fakes;

import marketsoft.dao.IMermaDAO;
import marketsoft.model.Merma;
import java.util.ArrayList;
import java.util.List;

public class FakeMermaDAO implements IMermaDAO {

    public boolean resultado = true;
    public int llamadasRegistrar = 0;
    public int ultimoIdProducto, ultimaCantidad;
    public String ultimoMotivo, ultimoDesde, ultimoHasta;
    public List<Merma> lista = new ArrayList<>();

    @Override
    public boolean registrarMerma(int idProducto, int cantidadUnidades, String motivo) {
        llamadasRegistrar++;
        ultimoIdProducto = idProducto;
        ultimaCantidad = cantidadUnidades;
        ultimoMotivo = motivo;
        return resultado;
    }

    @Override
    public List<Merma> listarPorRango(String desde, String hasta) {
        ultimoDesde = desde;
        ultimoHasta = hasta;
        return lista;
    }
}
