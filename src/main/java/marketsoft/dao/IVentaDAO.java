package marketsoft.dao;

import marketsoft.model.Venta;
import java.sql.SQLException;
import java.util.List;

/** Contrato de acceso a datos (el controller depende de esta interfaz, no de la clase concreta). */
public interface IVentaDAO {

    void registrarVenta(Venta venta) throws SQLException;

    List<Venta> listarVentas();

    boolean eliminarVenta(int idVenta) throws SQLException;

    double totalVentasDelMes();

    double totalVentasDelDia();

    int contarVentasDelDia();

    double ticketPromedioDia();

    double totalDiaPorMetodo(String metodoPago);

    double totalDigitalDia();

    double gananciaEstimadaDelDia();
}
