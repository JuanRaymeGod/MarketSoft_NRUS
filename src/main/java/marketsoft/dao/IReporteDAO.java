package marketsoft.dao;

import marketsoft.model.FilaReporte;
import marketsoft.model.FilaVenta;
import java.util.List;

/** Contrato de acceso a datos (el controller depende de esta interfaz, no de la clase concreta). */
public interface IReporteDAO {

    List<FilaReporte> rentabilidadPorRango(String desde, String hasta);

    List<FilaVenta> ventasConCostoPorRango(String desde, String hasta);
}
