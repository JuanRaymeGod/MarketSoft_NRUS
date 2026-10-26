package marketsoft.dao;
import marketsoft.config.ConexionSQLite;
import marketsoft.model.FilaReporte;
import marketsoft.model.FilaVenta;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAO implements IReporteDAO {

    @Override
    public List<FilaReporte> rentabilidadPorRango(String desde, String hasta) {
        List<FilaReporte> lista = new ArrayList<>();
        String sql = "SELECT p.nombre, SUM(dv.cantidad) AS cant, "
                + "SUM(dv.cantidad * COALESCE(dv.costo_unitario_compra, p.precio_compra_unitario)) AS costo, "
                + "SUM(dv.subtotal) AS venta "
                + "FROM detalle_ventas dv "
                + "JOIN ventas v ON dv.id_venta = v.id_venta "
                + "JOIN productos p ON dv.id_producto = p.id_producto "
                + "WHERE date(v.fecha_hora,'-5 hours') BETWEEN ? AND ? "
                + "GROUP BY p.id_producto ORDER BY p.nombre";
        try (PreparedStatement ps = ConexionSQLite.getConexion().prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FilaReporte f = new FilaReporte();
                    f.producto = rs.getString("nombre");
                    f.cantidad = rs.getInt("cant");
                    f.costoTotal = rs.getDouble("costo");
                    f.ventaTotal = rs.getDouble("venta");
                    f.gananciaNeta = f.ventaTotal - f.costoTotal;
                    lista.add(f);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Para el botón "Exportar CSV" de ReportesPanel: una fila por venta, con su costo
    // total calculado en la misma consulta (evita hacer una query por cada venta).
    @Override
    public List<FilaVenta> ventasConCostoPorRango(String desde, String hasta) {
        List<FilaVenta> lista = new ArrayList<>();
        String sql = "SELECT v.id_venta, v.fecha_hora, v.nombre_cliente, v.dni_cliente, v.metodo_pago, "
                + "v.total AS venta_total, "
                + "COALESCE(SUM(dv.cantidad * COALESCE(dv.costo_unitario_compra, p.precio_compra_unitario)), 0) AS costo_total "
                + "FROM ventas v "
                + "LEFT JOIN detalle_ventas dv ON dv.id_venta = v.id_venta "
                + "LEFT JOIN productos p ON dv.id_producto = p.id_producto "
                + "WHERE date(v.fecha_hora,'-5 hours') BETWEEN ? AND ? "
                + "GROUP BY v.id_venta ORDER BY v.fecha_hora";
        try (PreparedStatement ps = ConexionSQLite.getConexion().prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FilaVenta f = new FilaVenta();
                    f.idVenta = rs.getInt("id_venta");
                    f.fecha = rs.getString("fecha_hora");
                    f.cliente = rs.getString("nombre_cliente");
                    f.dni = rs.getString("dni_cliente");
                    f.metodoPago = rs.getString("metodo_pago");
                    f.ventaTotal = rs.getDouble("venta_total");
                    f.costoTotal = rs.getDouble("costo_total");
                    f.gananciaNeta = f.ventaTotal - f.costoTotal;
                    lista.add(f);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
