package marketsoft.controller;
import marketsoft.dao.ReporteDAO;
import marketsoft.dao.IReporteDAO;
import marketsoft.model.FilaReporte;
import marketsoft.model.FilaVenta;
import java.util.List;

public class ReporteController {
    private final IReporteDAO reporteDAO;

    public ReporteController() {
        this(new ReporteDAO());
    }

    public ReporteController(IReporteDAO reporteDAO) {
        this.reporteDAO = reporteDAO;
    }

    public List<FilaReporte> obtenerRentabilidad(String desde, String hasta) {
        return reporteDAO.rentabilidadPorRango(desde, hasta);
    }

    public List<FilaVenta> obtenerVentasConCosto(String desde, String hasta) {
        return reporteDAO.ventasConCostoPorRango(desde, hasta);
    }
}
