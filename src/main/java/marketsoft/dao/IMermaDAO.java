package marketsoft.dao;

import marketsoft.model.Merma;
import java.util.List;

/** Contrato de acceso a datos (el controller depende de esta interfaz, no de la clase concreta). */
public interface IMermaDAO {

    boolean registrarMerma(int idProducto, int cantidadUnidades, String motivo);

    List<Merma> listarPorRango(String desde, String hasta);
}
