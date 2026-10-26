package marketsoft.controller;

import marketsoft.dao.MermaDAO;
import marketsoft.dao.IMermaDAO;
import marketsoft.model.Merma;
import java.util.List;

public class MermaController {
    private final IMermaDAO dao;

    public MermaController() {
        this(new MermaDAO());
    }

    public MermaController(IMermaDAO dao) {
        this.dao = dao;
    }

    public boolean registrarMerma(int idProducto, int cantidadUnidades, String motivo) {
        if (cantidadUnidades <= 0) return false;
        return dao.registrarMerma(idProducto, cantidadUnidades, motivo);
    }

    public List<Merma> listarPorRango(String desde, String hasta) { return dao.listarPorRango(desde, hasta); }

    public double totalPerdida(List<Merma> lista) {
        return lista.stream().mapToDouble(Merma::getPerdidaTotal).sum();
    }
}
