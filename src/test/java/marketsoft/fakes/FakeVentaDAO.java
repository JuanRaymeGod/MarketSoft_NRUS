package marketsoft.fakes;

import marketsoft.dao.IVentaDAO;
import marketsoft.model.Venta;
import java.util.ArrayList;
import java.util.List;

public class FakeVentaDAO implements IVentaDAO {

    public final List<Venta> registradas = new ArrayList<>();
    public double totalMes = 0;
    public String ultimoMetodoConsultado;

    @Override public void registrarVenta(Venta venta) { registradas.add(venta); }
    @Override public List<Venta> listarVentas() { return new ArrayList<>(registradas); }
    @Override public boolean eliminarVenta(int idVenta) { return true; }
    @Override public double totalVentasDelMes() { return totalMes; }
    @Override public double totalVentasDelDia() { return 0; }
    @Override public int contarVentasDelDia() { return 0; }
    @Override public double ticketPromedioDia() { return 0; }

    @Override
    public double totalDiaPorMetodo(String metodoPago) {
        ultimoMetodoConsultado = metodoPago;
        return 0;
    }

    @Override public double totalDigitalDia() { return 0; }
    @Override public double gananciaEstimadaDelDia() { return 0; }
}
